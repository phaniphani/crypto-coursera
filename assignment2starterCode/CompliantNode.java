import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private final double p_graph;
    private final double p_malicious;
    private final double p_txDistribution;
    private final int numRounds;

    private Set<Transaction> pendingTx;
    private boolean[] followees;
    private boolean[] malicious;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
        this.malicious = new boolean[followees.length];
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTx = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        Set<Transaction> toBeSent = new HashSet<>(this.pendingTx);
        this.pendingTx.clear();
        return toBeSent;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        Set<Integer> sender = candidates.stream()
                                    .map(e -> e.sender)
                                    .collect(Collectors.toSet());
        for (int i = 0; i < this.followees.length; i++) {
            if (this.followees[i] && !sender.contains(i)) {
                this.malicious[i] = true;
            }
        }

       candidates.stream()
                .filter(c -> !this.malicious[c.sender])
                .forEach(c -> pendingTx.add(c.tx));
    }

}
