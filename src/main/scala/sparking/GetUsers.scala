package sparking

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.rdd.RDD
import org.apache.spark.serializer.KryoRegistrator
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext, SparkContext._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{Days, LocalDate}

case class Badge(userId: Int, name: String)

object Badge {
  val Regex = """^.* UserId="([0-9]+)" Name="([a-zA-Z0-9]+)".*$""".r

  def fromRow(row: String) = row match {
    case Regex(userId, name) => Some(Badge(userId.toInt, name))
    case _ => None
  }
}

case class User(id: Int, displayName: String, reputation: Int, activityDays: Int, badges: Seq[String] = Seq.empty)

object User {
  // This is never going to be the best regex ever seen, but it's good enough for this example.
  val Regex = """^.* Id="([0-9]+)" Reputation="([0-9]+)" CreationDate="([0-9]{4}-[0-9]{2}-[0-9]{2})T.*" DisplayName="(.+)" LastAccess.*$""".r
  val DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd")

  def fromRow(row: String) = row match {
    case Regex(id, reputation, activityDays, displayName) =>
      val activity = Days.daysBetween(
        LocalDate.parse(activityDays, DateTimeFormatter),
        LocalDate.now)
      Some(User(id.toInt, displayName, reputation.toInt, activity.getDays))
    case _ => None
  }
}

class MyKryoRegistrator extends KryoRegistrator {
  override def registerClasses(kryo: Kryo) {
    kryo.register(classOf[Badge])
    kryo.register(classOf[User])
  }
}

object GetUsersWithBadges {
  def main(args: Array[String]) {
    val badgesInputPath = "/tmp/spark/Badges.xml"
    val usersInputPath = "/tmp/spark/Users.xml"

    val conf = new SparkConf()
    conf.set("spark.kryo.registrator", "sparking.MyKryoRegistrator")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.set("spark.rdd.compress", "true")
    val sc: SparkContext = new SparkContext(conf)

    val badges = sc.textFile(badgesInputPath)
      .flatMap(Badge.fromRow)
      .map(b => b.userId -> b.name)
      .groupByKey

    val users = sc.textFile(usersInputPath)
      .flatMap(User.fromRow)
      .keyBy(_.id)

    val usersWithBadges = users.leftOuterJoin(badges).map {
      case (_, (user, Some(badges))) => user.copy(badges = badges.toSeq.distinct)
      case (_, (user, _)) => user
    }.keyBy(_.badges.size).sortByKey(ascending = false)

    usersWithBadges.take(10).foreach {
      case (n, u) =>
        println(s"$n | ${u.displayName} | ${u.badges.take(5).mkString(",")} ...")
    }
  }
}

