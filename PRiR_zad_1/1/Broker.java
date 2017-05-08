import java.util.concurrent.LinkedBlockingDeque;

public class Broker implements BrokerInterface {
	private Integer[] US;
	private LinkedBlockingDeque<Task> taskBufor = new LinkedBlockingDeque<Task>();

	final String PUSH = "push";
	final String PEEK = "peek";
	final String OK = "ok";
	final String ERR = "err";

	private Task TakeTask() {
		Task orderedTask = null;
		Boolean taskNotreturned;
		synchronized (taskBufor) {
			do {
				taskNotreturned = false;
				orderedTask = taskBufor.stream().filter(x -> US[x.UsId] != 1)
						.findFirst().orElse(null);
				if (orderedTask == null) {
					taskNotreturned = true;
					try {
						taskBufor.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					taskBufor.remove(orderedTask);
					Task finalOrderedTask = orderedTask;
					if (orderedTask.type == PEEK) {
						Task searchedTask = taskBufor
								.stream()
								.filter(x -> x.parcelId == finalOrderedTask.parcelId)
								.filter(x -> x.type == PUSH).findFirst()
								.orElse(null);
						if (searchedTask != null) {
							orderedTask.parcel = searchedTask.parcel;
							orderedTask.status = OK;
							taskBufor.remove(searchedTask);
							taskNotreturned = true;
						}
					} else {
						Task searchedTask = taskBufor
								.stream()
								.filter(x -> x.parcelId == finalOrderedTask.parcelId)
								.filter(x -> x.type == PEEK).findFirst()
								.orElse(null);
						if (searchedTask != null) {
							searchedTask.parcel = orderedTask.parcel;
							searchedTask.status = OK;
							taskBufor.remove(searchedTask);
							taskNotreturned = true;
						}
					}
				}
			} while (taskNotreturned);
			synchronized (US[orderedTask.UsId]) {
				US[orderedTask.UsId] = 1;
			}
			taskBufor.notifyAll();
		}
		return orderedTask;
	}

	@Override
	public void setNumberOfStorageUnits(int USs) {
		this.US = new Integer[USs];
		for (int i = 0; i < this.US.length; i++) {
			this.US[i] = 0;
		}
	}

	@Override
	public void addRobot(RobotInterface robot) {
		new Thread(new Runnable() {
			public void run() {
				Task taskToOperate = null;
				ParcelInterface peekParcel = null;
				do {
					taskToOperate = TakeTask();
					if (taskToOperate.type == PUSH) {
						robot.push(taskToOperate.parcel, taskToOperate.UsId);
						taskToOperate.status = OK;
					} else {
						try {
							peekParcel = robot.peek(taskToOperate.UsId,
									taskToOperate.parcelId);
							taskToOperate.parcel = peekParcel;
							taskToOperate.status = OK;
						} catch (NullPointerException ex) {
							taskToOperate.parcel = peekParcel;
							taskToOperate.status = OK;
						} catch (Exception e) {
							taskToOperate.status = ERR;
							try {
								taskBufor.putFirst(taskToOperate);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							synchronized (US[taskToOperate.UsId]) {
								US[taskToOperate.UsId] = 0;
							}
							return;
						}
					}
					synchronized (US[taskToOperate.UsId]) {
						US[taskToOperate.UsId] = 0;
					}
				} while (true);
			}
		}).start();
	}

	@Override
	public void push(ParcelInterface parcel, int USid) {
		Task task = new Task(PUSH, parcel, USid, parcel.getParcelID());
		synchronized (taskBufor) {
			Task searchedTask = taskBufor.stream()
					.filter(x -> x.parcelId == task.parcelId)
					.filter(x -> x.type == PEEK).findFirst().orElse(null);
			if (searchedTask != null) {
				taskBufor.remove(searchedTask);
				searchedTask.parcel = task.parcel;
				searchedTask.status = OK;
			} else {
				try {
					taskBufor.putLast(task);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			taskBufor.notifyAll();
		}
	}

	@Override
	public ParcelInterface peek(int USid, int parcelID) {
		Task task = new Task(PEEK, null, USid, parcelID);
		synchronized (taskBufor) {
			Task finalOrderedTask = task;
			Task searchedTask = taskBufor.stream()
					.filter(x -> x.parcelId == finalOrderedTask.parcelId)
					.filter(x -> x.type == PUSH).findFirst().orElse(null);
			if (searchedTask != null) {
				task.parcel = searchedTask.parcel;
				task.status = OK;
				taskBufor.remove(searchedTask);
				return task.parcel;
			} else {
				try {
					taskBufor.putLast(task);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			taskBufor.notifyAll();
		}

		do {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (task.status != OK);
		return task.parcel;
	}
}

class Task {
	public String type;
	public ParcelInterface parcel;
	Integer UsId;
	Integer parcelId;
	String status;

	public Task(String type, ParcelInterface parcel, Integer UsId,
			Integer parcelId) {
		this.type = type;
		this.parcel = parcel;
		this.UsId = UsId;
		this.parcelId = parcelId;
		this.status = "";
	}
}
