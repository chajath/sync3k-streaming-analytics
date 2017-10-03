package sync3k.analytics

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ BytesDeserializer, StringDeserializer }
import org.apache.kafka.common.utils.Bytes
import org.apache.spark.streaming.kafka010.{ ConsumerStrategies, KafkaUtils, LocationStrategies }
import org.apache.spark.streaming.{ Seconds, StreamingContext }
import org.apache.spark.{ SparkConf, SparkContext }
import org.json4s._
import org.json4s.native.JsonMethods._

object CountPerTopic {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("CountPerTopic").setMaster("local[1]")
    val sc = new SparkContext(conf)

    val ssc = new StreamingContext(sc, Seconds(10))

    val stream = KafkaUtils.createDirectStream(ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.SubscribePattern[Bytes, String](".*".r.pattern, collection.Map[String, Object](
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
        .countByKey()
        .foreach {
          case ((topic, actionType), count) => println(s"[${time.milliseconds}] $topic: $actionType: $count")
        }
    })

    ssc.start()

    try {
      ssc.awaitTermination()
    } finally {
      sc.stop()
    }
  }
}
