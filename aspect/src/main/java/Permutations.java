/**
 * @author epavlova
 * @version 26.10.2018
 */
public class Permutations {
    private int n;
    private StringBuilder finalString;
    private StringBuilder currentString;

    private Permutations(int n) {
        this.n = n;
    }

    private void init() {
        finalString = new StringBuilder();
        currentString = new StringBuilder();
        for (int i = 1; i <= n; i++) {
            currentString.append(i);
        }
        finalString.append(currentString).append("\n");
    }

    private void singlePermutaion(int level) {
        String end = currentString.substring(level);
        String begin = currentString.reverse().substring(n - level);
        currentString.delete(0, n).append(end).append(begin);
        finalString.append(begin).append((level >= 4) ? "\n" : " ");
    }

    private void solve() {
        init();
        runPermutations(n - 1);
    }

    private void runPermutations(int level) {
        if (level == 0) return;
        for (int i = 0; i < n - level; i++) {
            runPermutations(level - 1);
            singlePermutaion(level);
        }
        runPermutations(level - 1);
    }

    private void print() {
        System.out.println(finalString);
        System.out.println(currentString);
    }

    public static void main(String[] args) {
        Permutations permutations = new Permutations(6);
        permutations.solve();
        permutations.print();
    }


}
