package master;

public class WriteMsg {
	private long transactionId;
	private long timeStamp;
	private ReplicaLoc loc;

	public WriteMsg(long transactionId, long timeStamp, ReplicaLoc loc) {
		this.transactionId = transactionId;
		this.timeStamp = timeStamp;
		this.loc = loc;
	}

	public long getTransactionId() {
		return transactionId;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public ReplicaLoc getLoc() {
		return loc;
	}
}
