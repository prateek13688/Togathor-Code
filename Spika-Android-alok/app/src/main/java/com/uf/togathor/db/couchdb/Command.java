package com.uf.togathor.db.couchdb;

import java.io.IOException;

import org.json.JSONException;

public interface Command<T> {
	public T execute () throws JSONException, IOException, TogathorException, TogathorForbiddenException;
}
