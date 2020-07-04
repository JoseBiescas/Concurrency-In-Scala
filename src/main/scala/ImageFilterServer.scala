import java.awt.image.BufferedImage
import java.io.{BufferedWriter, File, FileWriter}

import akka.actor.Actor
import javax.imageio.ImageIO

import scala.collection.parallel._

case class medianFilterClass(img : BufferedImage, fileName: String)
case class parallelFilterClass(img : BufferedImage, fileName: String)

class ImageFilterServer extends Actor {
  private var image : BufferedImage = _
  private var file : File = new File("src/main/TimeResults/results.txt")
  private var bw = new BufferedWriter(new FileWriter(file))
  def receive : PartialFunction[Any, Unit] = {
    case medianFilterClass(img, fileName) =>
      val width = img.getWidth
      val height = img.getHeight
      this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
      val windHeight = 3
      val windWidth = 3
      val edgex = (windWidth/2).floor.toInt
      val edgey = (windHeight/2).floor.toInt
      val start = System.currentTimeMillis()
      Filter(img, width, height, edgex, edgey, windWidth, windHeight)

      val output = new File("src/main/outputImages/" + fileName)
      ImageIO.write(this.image, "png", output)

      this.bw.write(s"Execution time of median filter for " + fileName + " : " + s"${System.currentTimeMillis() - start}\n")

    case parallelFilterClass(img, fileName) =>
      var count = 1
      val windHeight = 3
      val windWidth = 3
      val edgex = (windWidth/2).floor.toInt
      val edgey = (windHeight/2).floor.toInt
      val width = img.getWidth
      val height = img.getHeight
      this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

      val start = System.currentTimeMillis()
        val threads = Array((img, width, height/2, edgex, edgey, windWidth, windHeight),
        (img, width, height, edgex,  height / 2 - 1, windWidth, windHeight)).toParArray
      threads.par.foreach(iter => {
         Filter(iter._1, iter._2, iter._3, iter._4, iter._5,iter._6,iter._7)
         val output = new File("src/main/outputImages/" + fileName)
         ImageIO.write(this.image, "png", output)
      })

      count+=1
      this.bw.write(s"Execution time of parallel filter for " + fileName + " : " + s"${System.currentTimeMillis()-start}\n")

    case _ =>
      this.bw.close()
      context.system.terminate()
  }

  def Filter(image: BufferedImage, width : Int, height : Int, edgex : Int, edgey : Int, windWidth: Int, windHeight: Int): Unit = {
    val window = Array.ofDim[Int](windHeight * windWidth)
    for (x <- edgex until width - edgex) {
      for (y <- edgey until height - 1) {
        var i = 0
        for (fx <- 0 until windWidth) {
          for (fy <- 0 until windHeight) {
            window(i) = image.getRGB(x + fx - edgex, y + fy - 1)
            i = i + 1
          }
        }
        scala.util.Sorting.quickSort(window)
        this.image.setRGB(x, y, window(windWidth * windHeight / 2))
      }
    }
  }
}
