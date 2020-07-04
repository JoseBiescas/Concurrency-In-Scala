import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import javax.imageio.ImageIO

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object MedianMain {
  def main(args: Array[String]): Unit ={
    val medianClient = MedianClient
    val Simulation = ActorSystem("MedianSystem")
    val medianServer = Simulation.actorOf(Props[ImageFilterServer], name = "medianServers")

    (medianClient.imgs).foreach(image => {
      var img = ImageIO.read(image)

      medianServer ! medianFilterClass(img, s"median-${image.getName}")

      medianServer ! parallelFilterClass(img, s"parallel-${image.getName}")

    })

    medianServer ! "Terminate actors"

    Await.ready(Simulation.whenTerminated, Duration.Inf)
    println("Filtering Complete, check the results in the file located in the TimeResults directory.")
    println("Check output images inside the outputImages directory.")
  }

}
