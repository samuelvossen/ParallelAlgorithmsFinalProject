package com.abc.handoff;

import com.abc.pp.stringhandoff.StringHandoff;
import com.programix.thread.ShutdownException;
import com.programix.thread.TimedOutException;

public class StringHandoffImpl implements StringHandoff {
	private String message;

	public StringHandoffImpl() {
		message = null;
	}

	@Override
	public synchronized void pass(String msg, long msTimeout)
			throws InterruptedException, TimedOutException, ShutdownException, IllegalStateException {

		if (message == null) {
			message = msg;
			notifyAll();
		}
		if (msTimeout == 0L) {
			while (message != null) {
				wait();
			}
			message = msg;
			notifyAll();
		}

		long endTime = System.currentTimeMillis() + msTimeout;
		long msRemaining = msTimeout;

		while (message != null && msRemaining > 0L) {
			wait(msRemaining);
			msRemaining = endTime - System.currentTimeMillis();
		}
		if (message == null) {
			message = msg;
			notifyAll();
		} else {
			throw new TimedOutException();
		}
	}

	@Override
	public synchronized void pass(String msg) throws InterruptedException, ShutdownException, IllegalStateException {

		while (message != null) {
			wait();
		}
		message = msg;
		notifyAll();
	}

	@Override
	public synchronized String receive(long msTimeout)
			throws InterruptedException, TimedOutException, ShutdownException, IllegalStateException {
		String receivedMessage;
		if (message != null) {
			receivedMessage = message;
			message = null;
			notifyAll();
			return receivedMessage;
		}
		if (msTimeout == 0L) {
			while (message == null) {
				wait();
			}
			receivedMessage = message;
			message = null;
			notifyAll();
			return receivedMessage;
		}

		long endTime = System.currentTimeMillis() + msTimeout;
		long msRemaining = msTimeout;

		while (message == null && msRemaining > 0L) {
			wait(msRemaining);
			msRemaining = endTime - System.currentTimeMillis();
		}
		if (message != null) {
			receivedMessage = message;
			message = null;
			notifyAll();
			return receivedMessage;
		}
		throw new TimedOutException();
	}

	@Override
	public synchronized String receive() throws InterruptedException, ShutdownException, IllegalStateException {

		while (message == null) {
			wait();
		}
		String receivedMessage = message;
		message = null;
		notifyAll();
		return receivedMessage;
	}

	@Override
	public synchronized void shutdown() {
		throw new RuntimeException("not implemented yet"); // FIXME
	}

	@Override
	public Object getLockObject() {
		return this;
	}
}