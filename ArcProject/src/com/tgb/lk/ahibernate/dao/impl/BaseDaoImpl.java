package com.tgb.lk.ahibernate.dao.impl;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tgb.lk.ahibernate.annotation.Column;
import com.tgb.lk.ahibernate.annotation.Id;
import com.tgb.lk.ahibernate.annotation.Table;
import com.tgb.lk.ahibernate.dao.BaseDao;
import com.tgb.lk.ahibernate.util.TableHelper;

/**
 * AHibernate概要 <br/>
 * (一)支持功能: 1.自动建表,支持属性来自继承类:可根据注解自动完成建表,并且对于继承类中的注解字段也支持自动建表. 2.自动支持增删改
 * ,增改支持对象化操作:增删改是数据库操作的最基本单元,不用重复写这些增删改的代码,并且添加和更新支持类似于hibernate中的对象化操作.
 * 3.查询方式灵活:支持android框架提供的方式,也支持原生sql方式.
 * 4.查询结果对象化:对于查询结果可自动包装为实体对象,类似于hibernate框架.
 * 5.查询结果灵活:查询结果支持对象化,也支持结果为List<Map<String,String>>形式,这个方法在实际项目中很实用,且效率更好些.
 * 6.日志较详细:因为android开发不支持热部署调试,运行报错时可根据日志来定位错误,这样可以减少运行Android的次数. <br/>
 * (二)不足之处: <br/>
 * 1.id暂时只支持int类型,不支持uuid,在sqlite中不建议用uuid.
 * 2.现在每个方法都自己开启和关闭事务,暂时还不支持在一个事务中做多个操作然后统一提交事务. <br/>
 * (三)作者寄语:<br/>
 * 昔日有JavaScript借Java发展,今日也希望AHibernate借Hibernate之名发展.
 * 希望这个项目以后会成为开源社区的重要一员,更希望这个项目能给所有Android开发者带便利.
 * 欢迎访问我的博客:http://blog.csdn.net/lk_blog,
 * 这里有这个框架的使用范例和源码,希望朋友们多多交流完善这个框架,共同推动中国开源事业的发展,AHibernate期待与您共创美好未来!!!
 */
public class BaseDaoImpl<T> implements BaseDao<T> {
	//private String TAG = "AHibernate";
	private SQLiteOpenHelper dbHelper;
	private String tableName;
	private String idColumn;
	private Class<T> clazz;
	private List<Field> allFields;
	private final Logger Log = Logger.getLogger("gis");
	@SuppressWarnings("unchecked")
	public BaseDaoImpl(SQLiteOpenHelper dbHelper) {
		this.dbHelper = dbHelper;

		this.clazz = ((Class<T>) ((java.lang.reflect.ParameterizedType) super
				.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);

		if (this.clazz.isAnnotationPresent(Table.class)) {
			Table table = (Table) this.clazz.getAnnotation(Table.class);
			this.tableName = table.name();
		}

		// 加载所有字段
		this.allFields = TableHelper.joinFields(this.clazz.getDeclaredFields(),
				this.clazz.getSuperclass().getDeclaredFields());

		// 找到主键
		for (Field field : this.allFields) {
			if (field.isAnnotationPresent(Id.class)) {
				Column column = (Column) field.getAnnotation(Column.class);
				this.idColumn = column.name();
				break;
			}
		}

		Log.debug( "clazz:" + this.clazz + " tableName:" + this.tableName
				+ " idColumn:" + this.idColumn);
	}

	public SQLiteOpenHelper getDbHelper() {
		return dbHelper;
	}

	public T get(int id) {
		String selection = this.idColumn + " = ?";
		String[] selectionArgs = { Integer.toString(id) };
		Log.debug("[get]: select * from " + this.tableName + " where "
				+ this.idColumn + " = '" + id + "'");
		List<T> list = find(null, selection, selectionArgs, null, null, null,
				null);
		if ((list != null) && (list.size() > 0)) {
			return (T) list.get(0);
		}
		return null;
	}

	public List<T> rawQuery(String sql, String[] selectionArgs) {
		Log.debug( "[rawQuery]: " + sql);

		List<T> list = new ArrayList<T>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.dbHelper.getReadableDatabase();
			cursor = db.rawQuery(sql, selectionArgs);

			getListFromCursor(list, cursor);
		} catch (Exception e) {
			Log.error("[rawQuery] from DB Exception.");
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return list;
	}

	public boolean isExist(String sql, String[] selectionArgs) {
		Log.debug("[isExist]: " + sql);

		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.dbHelper.getReadableDatabase();
			cursor = db.rawQuery(sql, selectionArgs);
			if (cursor.getCount() > 0) {
				return true;
			}
		} catch (Exception e) {
			Log.error( "[isExist] from DB Exception.");
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return false;
	}

	public List<T> find() {
		return find(null, null, null, null, null, null, null);
	}

	public List<T> find(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		Log.debug("[find]");

		List<T> list = new ArrayList<T>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.dbHelper.getReadableDatabase();
			cursor = db.query(this.tableName, columns, selection,
					selectionArgs, groupBy, having, orderBy, limit);

			getListFromCursor(list, cursor);
		} catch (Exception e) {
			Log.error("[find] from DB Exception");
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return list;
	}

	private void getListFromCursor(List<T> list, Cursor cursor)
			throws IllegalAccessException, InstantiationException {
		while (cursor.moveToNext()) {
			T entity = this.clazz.newInstance();

			for (Field field : this.allFields) {
				Column column = null;
				if (field.isAnnotationPresent(Column.class)) {
					column = (Column) field.getAnnotation(Column.class);

					field.setAccessible(true);
					Class<?> fieldType = field.getType();

					int c = cursor.getColumnIndex(column.name());
					if (c < 0) {
						continue; // 如果不存则循环下个属性值
					} else if ((Integer.TYPE == fieldType)
							|| (Integer.class == fieldType)) {
						field.set(entity, cursor.getInt(c));
					} else if (String.class == fieldType) {
						field.set(entity, cursor.getString(c));
					} else if ((Long.TYPE == fieldType)
							|| (Long.class == fieldType)) {
						field.set(entity, Long.valueOf(cursor.getLong(c)));
					} else if ((Float.TYPE == fieldType)
							|| (Float.class == fieldType)) {
						field.set(entity, Float.valueOf(cursor.getFloat(c)));
					} else if ((Short.TYPE == fieldType)
							|| (Short.class == fieldType)) {
						field.set(entity, Short.valueOf(cursor.getShort(c)));
					} else if ((Double.TYPE == fieldType)
							|| (Double.class == fieldType)) {
						field.set(entity, Double.valueOf(cursor.getDouble(c)));
					} else if (Blob.class == fieldType) {
						field.set(entity, cursor.getBlob(c));
					} else if(byte[].class==fieldType){
						field.set(entity, cursor.getBlob(c));
					}else if (Character.TYPE == fieldType) {
						String fieldValue = cursor.getString(c);

						if ((fieldValue != null) && (fieldValue.length() > 0)) {
							field.set(entity, Character.valueOf(fieldValue
									.charAt(0)));
						}
					}
				}
			}

			list.add((T) entity);
		}
	}

	public long insert(T entity) {
		Log.debug("[insert]: inset into " + this.tableName + " "
				+ entity.toString());
		SQLiteDatabase db = null;
		try {
			db = this.dbHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();
			setContentValues(entity, cv, "create");
			long row = db.insert(this.tableName, null, cv);
			return row;
		} catch (Exception e) {
			Log.error( "[insert] into DB Exception.");
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}

		return 0L;
	}

	public void delete(int id) {
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		String where = this.idColumn + " = ?";
		String[] whereValue = { Integer.toString(id) };

		Log.debug("[delete]: delelte from " + this.tableName + " where "
				+ where.replace("?", String.valueOf(id)));

		db.delete(this.tableName, where, whereValue);
		db.close();
	}

	public void delete(Integer... ids) {
		if (ids.length > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < ids.length; i++) {
				sb.append('?').append(',');
			}
			sb.deleteCharAt(sb.length() - 1);
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			String sql = "delete from " + this.tableName + " where "
					+ this.idColumn + " in (" + sb + ")";

			Log.debug("[delete]: " + sql);

			db.execSQL(sql, (Object[]) ids);
			db.close();
		}
	}

	public void update(T entity) {
		SQLiteDatabase db = null;
		try {
			db = this.dbHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();

			setContentValues(entity, cv, "update");

			String where = this.idColumn + " = ?";
			int id = Integer.parseInt(cv.get(this.idColumn).toString());
			cv.remove(this.idColumn);

			Log.debug("[update]: update " + this.tableName + " where "
					+ where.replace("?", String.valueOf(id)));

			String[] whereValue = { Integer.toString(id) };
			db.update(this.tableName, cv, where, whereValue);
		} catch (Exception e) {
			Log.error("[update] DB Exception.");
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
	}

	private void setContentValues(T entity, ContentValues cv, String type)
			throws IllegalAccessException {

		for (Field field : this.allFields) {
			if (!field.isAnnotationPresent(Column.class)) {
				continue;
			}
			Column column = (Column) field.getAnnotation(Column.class);

			field.setAccessible(true);
			Object fieldValue = field.get(entity);
			if (fieldValue == null)
				continue;
			if (("create".equals(type))
					&& (field.isAnnotationPresent(Id.class))) {
				continue;
			}
			//TODO changed by sdujq
			if(fieldValue instanceof byte[]){
				cv.put(column.name(), (byte[])fieldValue);
			}
			else {
				cv.put(column.name(), fieldValue.toString());}
			
		}
	}

	/**
	 * 将查询的结果保存为名值对map.
	 * 
	 * @param sql
	 *            查询sql
	 * @param selectionArgs
	 *            参数值
	 * @return 返回的Map中的key全部是小写形式.
	 */
	public List<Map<String, String>> query2MapList(String sql,
			String[] selectionArgs) {
		Log.debug("[query2MapList]: " + sql);
		SQLiteDatabase db = null;
		Cursor cursor = null;
		List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
		try {
			db = this.dbHelper.getReadableDatabase();
			cursor = db.rawQuery(sql, selectionArgs);
			while (cursor.moveToNext()) {
				Map<String, String> map = new HashMap<String, String>();
				for (String columnName : cursor.getColumnNames()) {
					map.put(columnName.toLowerCase(), cursor.getString(cursor
							.getColumnIndex(columnName)));
				}
				retList.add(map);
			}
		} catch (Exception e) {
			Log.error("[query2MapList] from DB exception");
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return retList;
	}

	/**
	 * 封装执行sql代码.
	 * 
	 * @param sql
	 * @param selectionArgs
	 */
	public void execSql(String sql, Object[] selectionArgs) {
		SQLiteDatabase db = null;
		Log.debug("[execSql]: " + sql);
		try {
			db = this.dbHelper.getWritableDatabase();
			if (selectionArgs == null) {
				db.execSQL(sql);
			} else {
				db.execSQL(sql, selectionArgs);
			}
		} catch (Exception e) {
			Log.error("[execSql] DB exception.");
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}
}