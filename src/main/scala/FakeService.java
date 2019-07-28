public class FakeService {

    public class E extends Exception {}
    public class CustomCheckedException extends Throwable {}
    public class CustomUncheckedException extends RuntimeException {}

    public void doSomethingWithCheckedExceptions() throws CustomCheckedException {
        throw new CustomCheckedException();
    }

    public void doSomethingWithUncheckedExceptions() {
        throw new CustomUncheckedException();
    }
}
