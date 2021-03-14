class Liveness {
    public static void main(String[] args) {
        System.out.println(new Doit().doit());
    }

}

class Doit {
    public int doit() {
        int p;
        int q;
        int x;
        int y;
        int m;
        int z;
        int k;
        int nouse1;
        int nouse2;
        x = p + 1;
        y = q + z;
        while (nouse1 < 1) {
            m = k;
            y = m - 1;
            if (nouse2 < 1) {
                x = 4;
                q = y;
            } else {
                x = x - 3;
            }
        }
        z = 2 * p;
        return 0;
    }
}
