package sparking

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{Days, LocalDate}

case class User(displayName: String, reputation: Int, activityDays: Int)

object User {

  // This is never going to be the best regex ever seen, but it's good enough for this example.
  val Regex = """^.*Reputation="([0-9]+)" CreationDate="([0-9]{4}-[0-9]{2}-[0-9]{2})T.*" DisplayName="(.+)" LastAccess.*$""".r
  val DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd")

  def fromRow(row: String) = row match {
    case Regex(reputation, activityDays, displayName) =>
      val activity = Days.daysBetween(LocalDate.parse(activityDays, DateTimeFormatter), LocalDate.now)
      Some(User(displayName, reputation.toInt, activity.getDays))
    case _ => None
  }
}

object GetUsers {

  type Reputation = Int

  def main(args: Array[String]) {
    val inputPath = "/tmp/spark/Users.xml"

    val sc: SparkContext = new SparkContext(new SparkConf())

    val users: RDD[(Reputation, User)] =
      sc.textFile(inputPath).
      map(User.fromRow).
      collect {
        case Some(user) => user.reputation -> user
      }.
      sortByKey(ascending = false)

      users.take(10).foreach(println)
  }
}

