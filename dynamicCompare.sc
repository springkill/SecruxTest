import java.nio.file.{Files, Paths}

@main def dynamicCompare(srcDir: String, traceFile: String, compareDir: String): Unit = {
  val srcPath     = Paths.get(srcDir).toAbsolutePath
  val comparePath = Paths.get(compareDir).toAbsolutePath
  val beforeDir   = comparePath.resolve("before")
  val afterDir    = comparePath.resolve("after")

  if (Files.exists(comparePath)) {
    better.files.File(comparePath).delete(true)
  }
  Files.createDirectories(beforeDir)
  Files.createDirectories(afterDir)

  workspace.projects
    .filter(_.inputPath == srcPath.toString)
    .map(_.name)
    .foreach(name => workspace.removeProject(name))

  println(s"[compare] importing code from $srcPath ...")
  importCode.java(srcPath.toString)

  println(s"[compare] dumping baseline graphs to $beforeDir ...")
  opts.dumpcpg14.outDir = beforeDir.toString
  run.dumpcpg14
  save

  println(s"[compare] applying dynamic overlay from $traceFile ...")
  opts.dynamicinvokelink.traceFile = traceFile
  opts.dynamicinvokelink.defaultEdgeType = io.shiftleft.codepropertygraph.generated.EdgeTypes.REACHING_DEF
  opts.dynamicinvokelink.createTags = true
  run.dynamicinvokelink

  println(s"[compare] dumping dynamic graphs to $afterDir ...")
  opts.dumpcpg14.outDir = afterDir.toString
  run.dumpcpg14
  save

  println("[compare] finished")
}
