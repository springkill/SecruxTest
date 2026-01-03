import io.shiftleft.codepropertygraph.cpgloading.CpgLoader
import io.shiftleft.semanticcpg.language._
import io.shiftleft.semanticcpg.language.dotextension._
import better.files.File

@main
def generate(): Unit = {
  val cpg = CpgLoader.load("/Users/z5642188/GithubProject/SecruxTest/workspace/SecruxTest/cpg.bin")
  try {
    val methods = cpg.method.l
    println(s"Total methods discovered: ${methods.size}")
    methods.take(20).foreach(m => println(s" - ${m.fullName}"))

    val mainMethods = methods.filter(_.name == "main").filter(_.filename.contains("SecruxMain"))

    if (mainMethods.isEmpty) {
      println("Main method not found")
    } else {
      println("Found main methods:")
      mainMethods.foreach(m => println(s" - ${m.fullName}"))

      val method = mainMethods.head
      val dotContent = method.dotCfg.mkString("\n")
      File("/Users/z5642188/GithubProject/SecruxTest/out/SecruxMain-main-cfg.dot").writeText(dotContent)
      println("CFG DOT exported to out/SecruxMain-main-cfg.dot")
    }
  } finally {
    cpg.close()
  }
}

