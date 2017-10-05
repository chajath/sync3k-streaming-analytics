package sync3k.analytics

import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerConfig, ProducerRecord }
import org.apache.kafka.common.serialization.{ BytesDeserializer, StringDeserializer, StringSerializer }
import org.apache.kafka.common.utils.Bytes
import org.apache.spark.streaming.kafka010.{ ConsumerStrategies, KafkaUtils, LocationStrategies }
import org.apache.spark.streaming.{ Seconds, StreamingContext }
import org.apache.spark.{ SparkConf, SparkContext }
import org.json4s._
import org.json4s.native.JsonMethods._

case class CountRecord(topic: String, actionType: String, count: Int) extends Serializable

object CountPerTopic {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("CountPerTopic").setMaster("local[1]")
    val sc = new SparkContext(conf)

    val ssc = new StreamingContext(sc, Seconds(10))

    val stream = KafkaUtils.createDirectStream(ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.SubscribePattern[Bytes, String]("^(?!analytics).*".r.pattern, collection.Map[String, Object](
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest",
        ConsumerConfig.GROUP_ID_CONFIG -> "analyticsId1",
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[BytesDeserializer],
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
      )))

    stream.foreachRDD((rdd, time) => {
      println("Hello! " + rdd)
      rdd.map((rec) => {
        // TODO(yiinho): move outside once https://github.com/json4s/json4s/issues/137 is fixed again.
        implicit val formats: DefaultFormats.type = DefaultFormats
        ((rec.topic(), (parse(rec.value()) \ "type").extractOrElse[String]("")), rec.value())
      })
        .aggregateByKey(0)((x, _) => x + 1, (x, y) => x + y)
        .map({
          case ((topic, actionType), count) => (time, CountRecord(topic, actionType, count))
        })
        .foreachPartition((counts) => {
          val kafkaTopic = "analytics-count"
          val props = new Properties()
          props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
          props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
          props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)

          val producer = new KafkaProducer[String, String](props)

          // TODO(yiinho): move outside once https://github.com/json4s/json4s/issues/137 is fixed again.
          implicit val formats: DefaultFormats.type = DefaultFormats

          counts.foreach((count) => {
            val message = new ProducerRecord[String, String](kafkaTopic, s"${count._1.milliseconds}",
              org.json4s.native.Serialization.write(count._2))
            producer.send(message)
          })

          producer.close()
        })
    })

    ssc.start()

    try {
      ssc.awaitTermination()
    } finally {
      sc.stop()
    }
  }
}
