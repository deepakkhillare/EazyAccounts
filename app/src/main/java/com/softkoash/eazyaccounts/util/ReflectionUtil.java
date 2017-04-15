package com.softkoash.eazyaccounts.util;

import android.database.Cursor;
import android.util.Log;

import com.softkoash.eazyaccounts.model.Account;
import com.softkoash.eazyaccounts.model.AccountGroup;
import com.softkoash.eazyaccounts.model.Product;
import com.softkoash.eazyaccounts.model.ProductGroup;
import com.softkoash.eazyaccounts.model.Unit;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by Deepak on 4/15/2017.
 */
public class ReflectionUtil {

    private static final String TAG = ReflectionUtil.class.getName();

    private static Map<Class, Map<String, Method>> classFieldsMap = new HashMap<>();

    public static Map<String, Method> getFields(Class<?> destType) {
        Map<String, Method> fieldMap = classFieldsMap.get(destType);
        if(null == fieldMap) {
            //load class fields using reflection
            Method[] methods = destType.getDeclaredMethods();
            fieldMap = new HashMap<>();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
                    fieldMap.put(method.getName().substring(3), method);
                }
            }
            classFieldsMap.put(destType, fieldMap);
        }
        return fieldMap;
    }

    public static RealmObject convertToRealm(Cursor cursor, int indexToStart, int indexToEnd, Class<?> destType, Realm realm) throws Exception {
        if (!RealmObject.class.isAssignableFrom(destType)) {
            throw new Exception("Cannot convert " + destType.getName() +" to RealmObject");
        }
        Map<String, Method> fieldMap = getFields(destType);
        //sqlite metadata
        String[] columnNames = cursor.getColumnNames();
        RealmObject destObj = null;
        try {
            destObj = (RealmObject) destType.newInstance();
            for (int i = indexToStart; i <= indexToEnd; i++) {
                try {
                    Method field = fieldMap.get(columnNames[i]);
                    Log.d(TAG, "columnName=" + columnNames[i] + " field=" + field);
                    field.invoke(destObj, getDesiredObject(cursor, i, field.getParameterTypes()[0], realm));
                } catch (Exception e) {
                    Log.e(TAG, "Error setting field for column: " + columnNames[i] + " on desttype: " + destType.getName());
                    throw e;
                }
            }
            fieldMap.get("CreatedDate").invoke(destObj, new Date());
            fieldMap.get("CreatedBy").invoke(destObj, SystemUtil.getDeviceId());
            Log.d(TAG, "Loaded object: " + destObj + " of type: " + destType.getName());
        } catch(Exception e) {
            Log.e(TAG, "Error converting object of type " + destType.getName(), e);
            throw e;
        }
        return destObj;
    }

    public static Object getDesiredObject(Cursor cursor, int index, Class<?> fieldType, Realm realm) {
        Object rv = null;
        if (String.class.isAssignableFrom(fieldType)) {
            rv = cursor.getString(index);
        } else if (Double.class.isAssignableFrom(fieldType)) {
            rv = cursor.getDouble(index);
        } else if (Integer.class.isAssignableFrom(fieldType)) {
            rv = cursor.getInt(index);
        } else if (Long.class.isAssignableFrom(fieldType)) {
            rv = cursor.getLong(index);
        } else if (Date.class.isAssignableFrom(fieldType)) {
            String dtStr = cursor.getString(index);
            if (null != dtStr && !dtStr.trim().isEmpty()) {
                try {
                    rv = Constants.SHORT_DATE_FORMAT.parse(dtStr);
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date value: " + dtStr + " for field: " + fieldType.getName(), e);
                }
            }
        } else if (Boolean.class.isAssignableFrom(fieldType)) {
            rv = cursor.getInt(index) == 1 ? Boolean.TRUE : Boolean.FALSE;
        } else if (AccountGroup.class.isAssignableFrom(fieldType)) {
            rv = realm.where(AccountGroup.class).equalTo("id", cursor.getInt(index)).findFirst();
        } else if(Product.class.isAssignableFrom(fieldType)) {
            rv = realm.where(Product.class).equalTo("id", cursor.getInt(index)).findFirst();
        } else if(Account.class.isAssignableFrom(fieldType)) {
            rv = realm.where(Account.class).equalTo("id", cursor.getInt(index)).findFirst();
        } else if(Unit.class.isAssignableFrom(fieldType)) {
            rv = realm.where(Unit.class).equalTo("id", cursor.getInt(index)).findFirst();
        } else if(ProductGroup.class.isAssignableFrom(fieldType)) {
            rv = realm.where(ProductGroup.class).equalTo("id", cursor.getInt(index)).findFirst();
        } else  {
            Log.e(TAG, "No type found for field type: " + fieldType.getName());
        }
        return rv;
    }

}
