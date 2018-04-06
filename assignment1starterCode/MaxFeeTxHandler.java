import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaxFeeTxHandler {

    private final UTXOPool utxoPool;

    public MaxFeeTxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        double max = 0;
        Map<Double, List<Transaction>> maxTx = new HashMap<>();
        for (Transaction tx : possibleTxs) {
            double inputSum = tx.getInputs().stream()
                                .mapToDouble(e -> (utxoPool.getTxOutput(new UTXO(e.prevTxHash, e.outputIndex))).value)
                                .sum();
            double outputSum = tx.getOutputs().stream()
                                .mapToDouble(e -> e.value)
                                .sum();
            double diff = inputSum - outputSum;
            if (!maxTx.containsKey(diff)) {
                maxTx.put(diff, new ArrayList<>());
            }
            maxTx.get(diff).add(tx);
            max = Math.max(max, diff);
        }
        Transaction[] transactions = new Transaction[maxTx.get(max).size()];
        List<Transaction> transactionList = maxTx.get(max);
        for (int i = 0; i < transactions.length; i++) {
            transactions[i] = transactionList.get(i);
        }
        return transactions;
    }
}
