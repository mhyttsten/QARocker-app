package com.pf.dedup;

public class DeltaReporter {
	
	private String _dEntityName;
	private long _dEntity;
	private String _dPayloadName;
	private long _dPayload;
	private String _dTimeInSName;
	private long _dTimeInMS;

	private long _startTimeMS;

	private long _nowEntity;
	private long _nowPayload;
	private long _nowTimeMS = System.currentTimeMillis();
	
	private long _lastREntity;
	private long _lastRPayload;
	private long _lastRTimeInMS = System.currentTimeMillis();
	
	public String report(long entity, long payload) {
		_nowEntity += entity;
		_nowPayload += payload;
		_nowTimeMS = System.currentTimeMillis();
		
		if (_nowEntity - _lastREntity > _dEntity) {
			return reportNow();
		}
		if (_nowPayload - _lastRPayload > _dPayload) {
			return reportNow();
		}
		if (_nowTimeMS - _lastRTimeInMS > _dTimeInMS) {
			return reportNow();
		}
		return null;
	}
	
	public long getNowEntities() { return _nowEntity; }
	public long getNowPayload() { return _nowPayload; }
	
	public String reportNow() {
		_lastREntity = _nowEntity;
		_lastRPayload = _nowPayload;
		_lastRTimeInMS = _nowTimeMS;
		
		return _dEntityName + ": " + String.valueOf(_nowEntity) + ", " +
			   _dPayloadName + ": " + getPayloadString() + ", " +
			   _dTimeInSName + ": " + String.valueOf((_nowTimeMS-_startTimeMS)/1000) + "s";
	}
	
	public String getPayloadString() {
		if (_nowPayload < 1024) {
			return String.valueOf(_nowPayload) + " bytes";
		} else if(_nowPayload < (1024*1024)) {
			return String.valueOf(_nowPayload/1024) + " kb";
		} else if(_nowPayload < (1024L*1024L*1024L)) {
			return String.valueOf(_nowPayload/1024/1024) + " MB";
		} else if(_nowPayload < (1024L*1024L*1024L*1024L)) {
			return String.valueOf(_nowPayload/1024/1024/1024) + " GB";
		}
		return String.valueOf(_nowPayload) + " bytes";
	}
	
	public void setInterval(
			String dEntityName,
			long dEntity,
			String dPayloadName, 
			long dPayload,
			String dTimeInSName,
			long dTimeInS) {
		_dEntityName = dEntityName;
		_dEntity = dEntity;
		_dPayloadName = dPayloadName;
		_dPayload = dPayload;
		_dTimeInSName = dTimeInSName;
		_dTimeInMS = dTimeInS * 1000;
		_startTimeMS = System.currentTimeMillis();
	}
}
