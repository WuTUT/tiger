class TestGc {
    public static void main(String[] a) {
        System.out.println(new B().start());
    }
}

class A {

    public int f() {

        int[] p;
        p = new int[20];

        return 0;
    }
}

class AA {
    int[] p;
    int a;
    int sa;
    int ad;
    A s;
    A ss1;

    public int f() {
        s = new A();
        ss1 = new A();
        sa = 2;
        // p = new int[11];
        return 2;
    }

}

class B {
    int znt;

    public int start() {
        int cnt;
        int dnt;
        cnt = 0;
        dnt = 0;
        znt = 0;
        while (dnt < 10) {
            while (cnt < dnt + znt) {
                System.out.println(new A().f());
                cnt = cnt + 1;
            }
            cnt = 0;
            while (cnt < dnt) {
                System.out.println(new AA().f());
                cnt = cnt + 1;
            }
            dnt = dnt + 1;
            znt = znt + 2;
        }

        return 0;
    }
}