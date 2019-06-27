package com.zk.cabinet.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.zk.cabinet.bean.Cabinet;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "Cabinet".
*/
public class CabinetDao extends AbstractDao<Cabinet, Long> {

    public static final String TABLENAME = "Cabinet";

    /**
     * Properties of entity Cabinet.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "ID");
        public final static Property CellNumber = new Property(1, int.class, "cellNumber", false, "CellNumber");
        public final static Property BoxName = new Property(2, String.class, "boxName", false, "BoxName");
        public final static Property Proportion = new Property(3, int.class, "proportion", false, "Proportion");
        public final static Property TargetAddress = new Property(4, int.class, "targetAddress", false, "TargetAddress");
        public final static Property TargetAddressForLight = new Property(5, int.class, "targetAddressForLight", false, "TargetAddressForLight");
        public final static Property SourceAddress = new Property(6, int.class, "sourceAddress", false, "SourceAddress");
        public final static Property LockNumber = new Property(7, int.class, "lockNumber", false, "LockNumber");
        public final static Property ReaderDeviceID = new Property(8, int.class, "readerDeviceID", false, "ReaderDeviceID");
        public final static Property AntennaNumber = new Property(9, String.class, "antennaNumber", false, "AntennaNumber");
        public final static Property SignBroken = new Property(10, int.class, "signBroken", false, "SignBroken");
    }


    public CabinetDao(DaoConfig config) {
        super(config);
    }
    
    public CabinetDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"Cabinet\" (" + //
                "\"ID\" INTEGER PRIMARY KEY ," + // 0: id
                "\"CellNumber\" INTEGER NOT NULL UNIQUE ," + // 1: cellNumber
                "\"BoxName\" TEXT," + // 2: boxName
                "\"Proportion\" INTEGER NOT NULL ," + // 3: proportion
                "\"TargetAddress\" INTEGER NOT NULL ," + // 4: targetAddress
                "\"TargetAddressForLight\" INTEGER NOT NULL ," + // 5: targetAddressForLight
                "\"SourceAddress\" INTEGER NOT NULL ," + // 6: sourceAddress
                "\"LockNumber\" INTEGER NOT NULL ," + // 7: lockNumber
                "\"ReaderDeviceID\" INTEGER NOT NULL ," + // 8: readerDeviceID
                "\"AntennaNumber\" TEXT," + // 9: antennaNumber
                "\"SignBroken\" INTEGER NOT NULL );"); // 10: signBroken
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"Cabinet\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Cabinet entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getCellNumber());
 
        String boxName = entity.getBoxName();
        if (boxName != null) {
            stmt.bindString(3, boxName);
        }
        stmt.bindLong(4, entity.getProportion());
        stmt.bindLong(5, entity.getTargetAddress());
        stmt.bindLong(6, entity.getTargetAddressForLight());
        stmt.bindLong(7, entity.getSourceAddress());
        stmt.bindLong(8, entity.getLockNumber());
        stmt.bindLong(9, entity.getReaderDeviceID());
 
        String antennaNumber = entity.getAntennaNumber();
        if (antennaNumber != null) {
            stmt.bindString(10, antennaNumber);
        }
        stmt.bindLong(11, entity.getSignBroken());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Cabinet entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getCellNumber());
 
        String boxName = entity.getBoxName();
        if (boxName != null) {
            stmt.bindString(3, boxName);
        }
        stmt.bindLong(4, entity.getProportion());
        stmt.bindLong(5, entity.getTargetAddress());
        stmt.bindLong(6, entity.getTargetAddressForLight());
        stmt.bindLong(7, entity.getSourceAddress());
        stmt.bindLong(8, entity.getLockNumber());
        stmt.bindLong(9, entity.getReaderDeviceID());
 
        String antennaNumber = entity.getAntennaNumber();
        if (antennaNumber != null) {
            stmt.bindString(10, antennaNumber);
        }
        stmt.bindLong(11, entity.getSignBroken());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Cabinet readEntity(Cursor cursor, int offset) {
        Cabinet entity = new Cabinet( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // cellNumber
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // boxName
            cursor.getInt(offset + 3), // proportion
            cursor.getInt(offset + 4), // targetAddress
            cursor.getInt(offset + 5), // targetAddressForLight
            cursor.getInt(offset + 6), // sourceAddress
            cursor.getInt(offset + 7), // lockNumber
            cursor.getInt(offset + 8), // readerDeviceID
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // antennaNumber
            cursor.getInt(offset + 10) // signBroken
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Cabinet entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setCellNumber(cursor.getInt(offset + 1));
        entity.setBoxName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setProportion(cursor.getInt(offset + 3));
        entity.setTargetAddress(cursor.getInt(offset + 4));
        entity.setTargetAddressForLight(cursor.getInt(offset + 5));
        entity.setSourceAddress(cursor.getInt(offset + 6));
        entity.setLockNumber(cursor.getInt(offset + 7));
        entity.setReaderDeviceID(cursor.getInt(offset + 8));
        entity.setAntennaNumber(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setSignBroken(cursor.getInt(offset + 10));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Cabinet entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Cabinet entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Cabinet entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
