package model;

import java.util.ArrayList;
import java.util.List;

import com.android.gers.shopping.list.ShoppingList;
import com.android.gers.shopping.list.DB.DbTableItems;
import com.android.gers.shopping.list.DB.DbTableLists;
import com.android.gers.shopping.list.DB.ShoppingListDb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ListDataSource {

	private ShoppingListDb dbHelper;
	private SQLiteDatabase db;
	
	public ListDataSource(Context context, ShoppingListDb dbHelper) {
		this.dbHelper = dbHelper;
	}

	public void open() throws SQLException {
		Log.i(ShoppingList.LOG_NAME, "Opening db");
		db = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		Log.i(ShoppingList.LOG_NAME, "Closing db");
		db.close();
	}
	
	public ShoppingListList createList(ShoppingListList newList) throws Exception {
		Log.i(ShoppingList.LOG_NAME, "Inserting new shopping list with name: " + newList.getName());
		
		Log.i(ShoppingList.LOG_NAME, "before insert list");
		long insertId = dbHelper.insertList(db, newList.getName());
		Log.i(ShoppingList.LOG_NAME, "after insert list");
		
		Cursor queryCursor = dbHelper.getListById(db, insertId);
		queryCursor.moveToFirst();
		if (queryCursor.isAfterLast()) {
			throw new Exception("We added an item but then it wasn't there!");
		}
		ShoppingListList retVal = cursorToShoppingList(queryCursor);
		Log.i(ShoppingList.LOG_NAME, "Inserted new list: " + retVal.toString());
		
		queryCursor.close();
		return retVal;
	}
	
	public ShoppingListItem createItem(ShoppingListItem newItem) throws Exception {
		Log.i(ShoppingList.LOG_NAME, "Inserting new item: " + newItem.toString());
		
		ContentValues kvps = shoppingListItemToContentValues(newItem);
		long insertId = dbHelper.insertItem(db, kvps);
		
		Cursor queryCursor = dbHelper.getItemsByID(db, insertId);
		
		queryCursor.moveToFirst();
		if (queryCursor.isAfterLast()) {
			throw new Exception("We added an item but then it wasn't there!");
		}
		ShoppingListItem retVal = cursorToShoppingListItem(queryCursor);
		Log.i(ShoppingList.LOG_NAME, "Inserted new item: " + retVal.toString());
		
		queryCursor.close();
		return retVal;
	}
	
	public Boolean updateItem(ShoppingListItem editItem) throws Exception {
		Log.i(ShoppingList.LOG_NAME, "Updating item: " + editItem.toString());
		
		ContentValues kvps = shoppingListItemToContentValues(editItem);
		Boolean retVal = dbHelper.updateItem(db, editItem.getId(), kvps);
		
		Log.i(ShoppingList.LOG_NAME, "Updated item");
		return retVal;
	}
	
	public void deleteList(ShoppingListList deleteList) {
		Log.i(ShoppingList.LOG_NAME, "About to delete this shopping list: " + deleteList.toString());
		
		if(dbHelper.deleteListById(db, deleteList.getId())) {
			Log.i(ShoppingList.LOG_NAME, "Successfully deleted list");
		} else {
			Log.e(ShoppingList.LOG_NAME, "Couldn't delete list with id " + deleteList.getId() + "!");
		}
	}
	
	public void deleteItem(ShoppingListItem deleteItem) {
		if(dbHelper.deleteItemById(db, deleteItem.getId())) {
			Log.i(ShoppingList.LOG_NAME, "Successfully deleted item");
		} else {
			Log.e(ShoppingList.LOG_NAME, "Couldn't delete item with id " + deleteItem.getId() + "!");
		}
	}
	
	public List<ShoppingListList> getShoppingLists(Boolean noComplete) {
		List<ShoppingListList> lists = new ArrayList<ShoppingListList>();
		Cursor queryCursor = dbHelper.getAllLists(db, noComplete);
		
		queryCursor.moveToFirst();
		while(!queryCursor.isAfterLast()) {
			lists.add(cursorToShoppingList(queryCursor));
			queryCursor.moveToNext();
		}
		queryCursor.close();
		
		return lists;
	}

	public ShoppingListList getShoppingList(long id) {
		Cursor queryCursor = dbHelper.getListById(db, id);
		
		ShoppingListList retVal = null;
		
		queryCursor.moveToFirst();
		if(!queryCursor.isAfterLast()) {
			retVal = cursorToShoppingList(queryCursor);
			queryCursor.moveToNext();
		}
		queryCursor.close();
		
		return retVal;
	}
	
	public List<ShoppingListItem> getShoppingListItems(long listId, Boolean noComplete) {
		List<ShoppingListItem> items = new ArrayList<ShoppingListItem>();
		Cursor queryCursor = dbHelper.getItemsByListID(db, listId, noComplete);
		
		queryCursor.moveToFirst();
		while(!queryCursor.isAfterLast()) {
			items.add(cursorToShoppingListItem(queryCursor));
			queryCursor.moveToNext();
		}
		queryCursor.close();
		
		return items;
	}
	
	private static ContentValues shoppingListItemToContentValues(ShoppingListItem item) {
		ContentValues kvps = new ContentValues();
		kvps.put(DbTableItems.COL_LIST_ID, item.getListId());
		kvps.put(DbTableItems.COL_NAME, item.getName());
		kvps.put(DbTableItems.COL_QUANTITY, item.getQuantity());
		kvps.put(DbTableItems.COL_QUANTITY_TYPE, item.getQuantityType().toString());
		kvps.put(DbTableItems.COL_IS_COMPLETE, item.getComplete());
		return kvps;
	}
	
	private static ShoppingListList cursorToShoppingList(Cursor cursor) {
		return new ShoppingListList(
				cursor.getInt(DbTableLists.COL_IDX_ID), 
				cursor.getString(DbTableLists.COL_IDX_NAME),
				cursor.getString(DbTableLists.COL_IDX_CREATION_DATE),
				cursor.getInt(DbTableLists.COL_IDX_IS_COMPLETE) > 0);
	}

	private static ShoppingListItem cursorToShoppingListItem(Cursor cursor) {
		return new ShoppingListItem(
				cursor.getInt(DbTableItems.COL_IDX_ID),
				cursor.getInt(DbTableItems.COL_IDX_LIST_ID),
				cursor.getString(DbTableItems.COL_IDX_NAME),
				cursor.getString(DbTableItems.COL_IDX_QUANTITY),
				QuantityType.fromString(cursor.getString(DbTableItems.COL_IDX_QUANTITY_TYPE)),
				cursor.getInt(DbTableItems.COL_IDX_IS_COMPLETE) > 0);
	}
	
}
