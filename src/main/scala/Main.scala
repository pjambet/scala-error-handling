object Main {

  def main(args: Array[String]): Unit = {
    val fs = new FakeService()
    fs.doSomethingWithUncheckedExceptions() // Compiles and throws a CustomUncheckedException
    fs.doSomethingWithCheckedExceptions() // Compiles and throws a CustomCheckedException
  }

}
