package com.uf.togathor.db.couchdb;

public interface ResultListener<T> {
	public void onResultsSucceeded(T result);
	public void onResultsFail();
}
