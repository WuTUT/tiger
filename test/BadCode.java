class BadCode {
    public static void main(String[] a) {
        System.out.println(new InnerBadCode().f());

    }
}

class InnerBadCode {

    int va1;

    public int f() {
        int va2;
        va3 = 3;
        bb = this.g();
        a = this.f(2);

        if (false < 11) {
            va2 = 3;
        } else {
            va2 = false;
        }
        return 2;
    }

}