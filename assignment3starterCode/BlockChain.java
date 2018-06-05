// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    List<Block> blocks = new ArrayList<>();
    TransactionPool transactionPool = new TransactionPool();
    TxHandler txHandler;
    UTXOPool utxoPool;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        utxoPool = new UTXOPool();
        txHandler = new TxHandler(utxoPool);
        addCoinbaseToUTXO(genesisBlock.getCoinbase());
        addBlock(genesisBlock);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return blocks.get(blocks.size() - 1);
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return this.utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return transactionPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        if (blocks.size() > CUT_OFF_AGE + 1)
            return false;
        Transaction[] transactions = new Transaction[block.getTransactions().size()];
        Transaction[] validTx = txHandler.handleTxs(block.getTransactions().toArray(transactions));
        if (validTx.length == block.getTransactions().size()) {
            addCoinbaseToUTXO(block.getCoinbase());
            blocks.add(block);
            return true;
        }
        return false;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        for (Block block : blocks) {
            for(Transaction transaction : block.getTransactions()) {
                if (transaction == tx)
                    return;
            }
        }
        this.transactionPool.addTransaction(tx);
    }

    private void addCoinbaseToUTXO(Transaction coinbase) {
        for (int i = 0; i < coinbase.numOutputs(); i++) {
            UTXO utxo = new UTXO(coinbase.getHash(), i);
            this.utxoPool.addUTXO(utxo, coinbase.getOutput(i));
        }
    }

}