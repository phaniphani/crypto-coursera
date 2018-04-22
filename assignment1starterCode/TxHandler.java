import java.util.HashSet;
import java.util.Set;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    private final UTXOPool utxoPool;

    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        double inputSum = 0;
        double outputSum = 0;
        Set<UTXO> utxoSet = new HashSet<>();
        if (tx.getInputs() != null) {
            int index = 0;
            for (Transaction.Input input : tx.getInputs()) {
                UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                if(!utxoPool.contains(utxo)) return false;
                if (utxoSet.contains(utxo)) return false;
                utxoSet.add(utxo);
                if (!Crypto.verifySignature(utxoPool.getTxOutput(utxo).address,
                                            tx.getRawDataToSign(index), input.signature))
                    return false;

                inputSum += utxoPool.getTxOutput(utxo).value;
                index += 1;
            }
        }
        for (Transaction.Output output : tx.getOutputs()) {
            if (output.value < 0) return false;
            outputSum += output.value;
        }
        int nums[] = new int[4];
        for (int num : nums) {

        }
        return inputSum >= outputSum;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        Set<Transaction> validTransactions = new HashSet<>();

        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {
                int index = 0;
                validTransactions.add(tx);
                for (Transaction.Input input : tx.getInputs()) {
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    utxoPool.removeUTXO(utxo);
                }
                for (Transaction.Output output : tx.getOutputs()) {
                    utxoPool.addUTXO(new UTXO(tx.getHash(), index++), output);
                }
            }
        }
        Transaction[] transactions = new Transaction[validTransactions.size()];
        return validTransactions.toArray(transactions);
    }

}
