package com.tcl.contacts.util;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import com.android.contacts.editor.AggregationSuggestionEngine.RawContact;
import com.android.contacts.interactions.ContactDeletionInteraction;

import java.util.ArrayList;
import java.util.HashSet;

/*
 * Author:gangzhou.qi
 * Create Date:2012/12/4
 */

//This class is created to merge repeated contacts in background when some import operations
//completed.It's called in the PeopleActivity.java

public class MergeRepeatedContacts {
    
    private static final String TAG = MergeRepeatedContacts.class.getSimpleName();
    private ContentResolver mContentResolver;
    private HashSet<Uri> repeatedContactsUriList = new HashSet<Uri>();
    Uri[] uri;
    
    public MergeRepeatedContacts(Context context){
        mContentResolver = context.getContentResolver();
    }
    
    public Uri[] queryRepeatedContacts(){
        startCompare();
        return uri;
    }
    
    //startCompare . Include name, account_name, account_type.
    private void startCompare(){
        String displayName1 = "Please... you are so lucky!", displayName2 = null;
        String accountType1 = "accountType1", accountType2 = null;
        String accountName1 = "accountName1", accountName2 = null;
        Long contactId1 = null, contactId2 = null;
        Cursor cursor = mContentResolver.query(RawContacts.CONTENT_URI, new String[]{RawContacts.CONTACT_ID, RawContacts.DISPLAY_NAME_PRIMARY, RawContacts.ACCOUNT_TYPE, RawContacts.ACCOUNT_NAME}, 
                RawContacts.DELETED + " =0", null, RawContacts.SORT_KEY_PRIMARY);
        Log.d("^^", "start compare and cursor.getcount:" + cursor.getCount());
        if(cursor !=null){
            while(cursor.moveToNext()){
                displayName2 = cursor.getString(cursor.getColumnIndex(RawContacts.DISPLAY_NAME_PRIMARY));
                accountType2 = cursor.getString(cursor.getColumnIndex(RawContacts.ACCOUNT_TYPE));
                accountName2 = cursor.getString(cursor.getColumnIndex(RawContacts.ACCOUNT_NAME));
                contactId2 = cursor.getLong(cursor.getColumnIndex(RawContacts.CONTACT_ID));
                if((displayName1 == null && displayName2 == null) || (displayName2 != null && displayName2.equals(displayName1))){
                    if((accountType1 == null && accountType2 == null) || (accountType2 != null && accountType2.equals(accountType1))){
                        if((accountName1 == null && accountName2 == null) || (accountName2 != null && accountName2.equals(accountName1))){
                            Log.d("^^", "compare name completed!");
                            comparePhone(new long[]{contactId1, contactId2});
                        }
                    }
                }
                displayName1 = displayName2;
                accountType1 = accountType2;
                accountName1 = accountName2;
                contactId1 = contactId2;
            }
            try {
                cursor.close();
            } catch (Exception e) {
                Log.d(TAG, "cursor is null when startCompare!");
            }
        }
    }
    
    //compare the phone item for the two contacts;
    private void comparePhone(long[] contactsIds){
        int phoneType1 = 0, phoneType2 = 0;
        String phoneNumber1 = null, phoneNumber2 = null;
        String[] projection = new String[]{Phone._ID, Phone.NUMBER, Phone.TYPE};
        
        Cursor cursor1 = mContentResolver.query(Phone.CONTENT_URI, projection,
                Phone.MIMETYPE + "=? AND " + Phone.RAW_CONTACT_ID + "=?",
                new String[]{Phone.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[0]))}, Phone.TYPE);
        Cursor cursor2 = mContentResolver.query(Phone.CONTENT_URI, projection,
                Phone.MIMETYPE + "=? AND " + Phone.RAW_CONTACT_ID + "=?",
                new String[]{Phone.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[1]))}, Phone.TYPE);
        
        if(cursor1.getCount() == cursor2.getCount()){
            Log.d("^^", "Number count is the same:" + cursor2.getCount() );
            if(cursor2.getCount() == 0){
                Log.d("^^", "compare phone completed!");
                comparePhoto(contactsIds);
                return;
            }
        for (int i = 0; i < cursor2.getCount() ; i++){
            cursor1.moveToNext();
            phoneType1 = cursor1.getInt(cursor1.getColumnIndex(Phone.TYPE));
            phoneNumber1 = cursor1.getString(cursor1.getColumnIndex(Phone.NUMBER));
            cursor2.moveToNext();
            phoneType2 = cursor2.getInt(cursor2.getColumnIndex(Phone.TYPE));
            phoneNumber2 = cursor2.getString(cursor2.getColumnIndex(Phone.NUMBER));
            
            if(phoneType1 == phoneType2){
                if((phoneNumber1 == null && phoneNumber2 == null) ||(phoneNumber2 != null && phoneNumber2.equals(phoneNumber1))){
                    if(i == cursor2.getCount() -1){
                        try {
                            cursor1.close();
                            cursor2.close();
                        } catch (Exception e) {
                            Log.d(TAG, "cursor is null when comparePhone!");
                        }
                        Log.d("^^", "compare phone completed!");
                        comparePhoto(contactsIds);
                        return;
                    }
                }else{
                    return;
                }
            }else{
                return;
            }
            
        }
        }
    }
    
    private void comparePhoto(long[] contactsIds){
        long photoId1 = 0, photoId2 = 0;
        
        String[] projection = new String[]{Contacts._ID, Contacts.PHOTO_ID};
        
        Cursor cursor = mContentResolver.query(Contacts.CONTENT_URI, projection, Contacts._ID + "=" + contactsIds[0], null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                photoId1 = cursor.getLong(cursor.getColumnIndex(Contacts.PHOTO_ID));
            }
            cursor.close();
        }
        
        cursor = mContentResolver.query(Contacts.CONTENT_URI, projection, Contacts._ID + "=" + contactsIds[1], null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                photoId2 = cursor.getLong(cursor.getColumnIndex(Contacts.PHOTO_ID));
            }
            cursor.close();
        }
        if(photoId1 == photoId2){
            Log.d("^^", "compare photo completed!");
            compareRingtone(contactsIds);
        }
    }
    
    private void compareRingtone(long[] contactsIds){
        String[] projection = new String[]{Contacts._ID, Contacts.CUSTOM_RINGTONE};
        
        String ringtone1 = null, ringtone2 = null;
        
        Cursor cursor = mContentResolver.query(Contacts.CONTENT_URI, projection, Contacts._ID + "=" + contactsIds[0], null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                ringtone1 = cursor.getString(cursor.getColumnIndex(Contacts.CUSTOM_RINGTONE));
            }
            cursor.close();
        }
        
        cursor = mContentResolver.query(Contacts.CONTENT_URI, projection, Contacts._ID + "="
                + contactsIds[1], null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                ringtone2 = cursor.getString(cursor.getColumnIndex(Contacts.CUSTOM_RINGTONE));
            }
            cursor.close();
        }
        if((ringtone1 == null && ringtone2 == null) ||(ringtone2 != null && ringtone2.equals(ringtone1))){
            Log.d("^^", "compare ringtone completed!");
            compareNote(contactsIds);
        }
            
    }
    private void compareNote(long[] contactsIds){
        String note1 = null, note2 = null;
        
        String[] projection = new String[]{Note._ID, Note.NOTE};
        
        Cursor cursor = mContentResolver.query(Data.CONTENT_URI, projection,
                Note.MIMETYPE + "=? AND " + Note.RAW_CONTACT_ID + "=?",
                new String[]{Note.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[0]))}, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                note1 = cursor.getString(cursor.getColumnIndex(Note.NOTE));
            }
            cursor.close();
        }
        
        cursor = mContentResolver.query(Data.CONTENT_URI, projection,
                Note.MIMETYPE + "=? AND " + Note.RAW_CONTACT_ID + "=?",
                new String[] {
                        Note.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[1]))
                }, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                note2 = cursor.getString(cursor.getColumnIndex(Note.NOTE));
            }
            cursor.close();
        }
        if((note1 == null && note2 == null) || ( note2 != null && note2.equals(note1))){
            Log.d("^^", "compare note completed!");
            compareNickname(contactsIds);
        }
    }
    private void compareNickname(long[] contactsIds){
        int typeMerged1 = 0, typeMerged2 = 0;
        String nickname1 = null, nickname2 = null;
        
        String[] projection = new String[]{Nickname._ID, Nickname.NAME, Nickname.TYPE};
        
        
        Cursor cursor = mContentResolver.query(Data.CONTENT_URI, projection,
                Nickname.MIMETYPE + "=? AND " + Nickname.RAW_CONTACT_ID + "=?",
                new String[]{Nickname.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[0]))}, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                nickname1 = cursor.getString(cursor.getColumnIndex(Nickname.NAME));
                typeMerged1 = cursor.getInt(cursor.getColumnIndex(Nickname.TYPE));
            }
            cursor.close();
        }
        
        cursor = mContentResolver.query(Data.CONTENT_URI,projection,
                Nickname.MIMETYPE + "=? AND " + Nickname.RAW_CONTACT_ID + "=?",
                new String[] {Nickname.CONTENT_ITEM_TYPE,String.valueOf(getRawContactId(contactsIds[1]))}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                nickname2 = cursor.getString(cursor.getColumnIndex(Nickname.NAME));
                typeMerged2 = cursor.getInt(cursor.getColumnIndex(Nickname.TYPE));
            }
            cursor.close();
        }
        if(typeMerged1 == typeMerged2){
            if((nickname1 == null && nickname2 == null) || (nickname2 != null && nickname2.equals(nickname1))){
            Log.d("^^", "compare nickname completed!");
            compareSipAddress(contactsIds);
            }
        }
    }
    private void compareSipAddress(long[] contactsIds){
        String sipAddress1 = null, sipAddress2 = null;
        
        String[] projection = new String[]{SipAddress._ID, SipAddress.SIP_ADDRESS};
        
        
        Cursor cursor = mContentResolver.query(Data.CONTENT_URI, projection,
                SipAddress.MIMETYPE + "=? AND " + SipAddress.RAW_CONTACT_ID + "=?",
                new String[]{SipAddress.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[0]))}, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                sipAddress1 = cursor.getString(cursor.getColumnIndex(SipAddress.SIP_ADDRESS));
            }
            cursor.close();
        }
        
        cursor = mContentResolver.query(Data.CONTENT_URI, projection,
                SipAddress.MIMETYPE + "=? AND " + SipAddress.RAW_CONTACT_ID + "=?",
                new String[]{SipAddress.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[1]))}, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                sipAddress2 = cursor.getString(cursor.getColumnIndex(SipAddress.SIP_ADDRESS));
            }
            cursor.close();
        }
        if((sipAddress1 == null && sipAddress2 ==null) || (sipAddress2 != null && sipAddress2.equals(sipAddress1))){
            Log.d("^^", "compare sipaddress completed!");
            compareEmail(contactsIds);
        }
    }
    private void compareEmail(long[] contactsIds){
        String[] projection = new String[] {Email._ID, Email.DATA, Email.TYPE};
        String email1 = null, email2 = null;
        int emailType1 =0, emailType2 = 0;
        
        Cursor cursor1 = mContentResolver.query(Email.CONTENT_URI, projection,
                Email.MIMETYPE + "=? AND " + Email.RAW_CONTACT_ID + "=?",
                new String[]{Email.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[0]))}, Email.TYPE);
        Cursor cursor2 = mContentResolver.query(Email.CONTENT_URI, projection,
                Email.MIMETYPE + "=? AND " + Email.RAW_CONTACT_ID + "=?",
                new String[]{Email.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[1]))}, Email.TYPE);
        if(cursor1.getCount() == cursor2.getCount()){
            if(cursor2.getCount() == 0){
                Log.d("^^", "compare email completed!");
                compareStructuredPostal(contactsIds);
                return;
            }
            for (int i = 0; i < cursor2.getCount() ; i++){
                cursor1.moveToNext();
                emailType1 = cursor1.getInt(cursor1.getColumnIndex(Phone.TYPE));
                email1 = cursor1.getString(cursor1.getColumnIndex(Phone.NUMBER));
                cursor2.moveToNext();
                emailType2 = cursor2.getInt(cursor2.getColumnIndex(Phone.TYPE));
                email2 = cursor2.getString(cursor2.getColumnIndex(Phone.NUMBER));
                
                if(emailType1 == emailType2){
                    if((email1 == null && email2 == null) ||(email2 != null && email2.equals(email1))){
                        if(i == cursor2.getCount() -1){
                            try {
                                cursor1.close();
                                cursor2.close();
                            } catch (Exception e) {
                                Log.d(TAG, "cursor is null when compareEmail!");
                            }
                            Log.d("^^", "compare email completed!");
                            compareStructuredPostal(contactsIds);
                            return;
                        }
                    }else{
                        return;
                    }
                }else{
                    return;
                }
            }
        }else{
            try {
                cursor1.close();
                cursor2.close();
            } catch (Exception e) {
                Log.d(TAG, "cursor is null when compareEmail!");
            }
        }
        
    }
    private void compareStructuredPostal(long[] contactsIds){
        String[] projection = new String[]{
                StructuredPostal._ID, StructuredPostal.STREET,
                StructuredPostal.CITY, StructuredPostal.REGION,
                StructuredPostal.POSTCODE, StructuredPostal.FORMATTED_ADDRESS,
                StructuredPostal.TYPE};
        
        String formattedAddress1 =null, formattedAddress2 =null;
        Cursor cursor1 = mContentResolver.query(StructuredPostal.CONTENT_URI, projection,
                StructuredPostal.MIMETYPE + "=? AND " + StructuredPostal.RAW_CONTACT_ID + "=?",
                new String[]{StructuredPostal.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[0]))}, StructuredPostal.TYPE);
        Cursor cursor2 = mContentResolver.query(StructuredPostal.CONTENT_URI, projection,
                StructuredPostal.MIMETYPE + "=? AND " + StructuredPostal.RAW_CONTACT_ID + "=?",
                new String[]{StructuredPostal.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[1]))}, StructuredPostal.TYPE);
        if(cursor1.getCount() == cursor2.getCount()){
            if(cursor2.getCount() == 0){
                Log.d("^^", "compare structurepostal completed!");
                compareOrganization(contactsIds);
                return;
            }
            for (int i = 0; i < cursor2.getCount() ; i++){
                cursor1.moveToNext();
                formattedAddress1 = cursor1.getString(cursor1.getColumnIndex(StructuredPostal.FORMATTED_ADDRESS));
                cursor2.moveToNext();
                formattedAddress2 = cursor2.getString(cursor2.getColumnIndex(StructuredPostal.FORMATTED_ADDRESS));
                
                    if((formattedAddress2 == null && formattedAddress1 == null) ||(formattedAddress2 != null && formattedAddress2.equals(formattedAddress1))){
                        if(i == cursor2.getCount() -1){
                            try {
                                cursor1.close();
                                cursor2.close();
                            } catch (Exception e) {
                                Log.d(TAG, "cursor is null when compareStructuredPostal!");
                            }
                            Log.d("^^", "compare structurepostal completed!");
                            compareOrganization(contactsIds);
                            return;
                        }
                    }else{
                        return;
                    }
            }
        }else{
            try {
                cursor1.close();
                cursor2.close();
            } catch (Exception e) {
                Log.d(TAG, "cursor is null when compareStructuredPostal!");
            }
        }
    }
    private void compareOrganization(long[] contactsIds){
        String[] projection = new String[]{
                Organization._ID, Organization.COMPANY,
                Organization.TITLE, Organization.TYPE};
        String company1 =null, company2 =null;
        String title1 =null, title2 =null;
        int type1 =0, type2 =0;
        
        Cursor cursor1 = mContentResolver.query(Data.CONTENT_URI, projection,
                Organization.MIMETYPE + "=? AND " + Organization.RAW_CONTACT_ID + "=?",
                new String[]{Organization.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[0]))}, Organization.TYPE);
        Cursor cursor2 = mContentResolver.query(Data.CONTENT_URI, projection,
                Organization.MIMETYPE + "=? AND " + Organization.RAW_CONTACT_ID + "=?",
                new String[]{Organization.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[1]))}, Organization.TYPE);
        if(cursor1.getCount() == cursor2.getCount()){
            if(cursor2.getCount() == 0){
                Log.d("^^", "compare organization completed!");
                compareIm(contactsIds);
            }
            for (int i = 0; i < cursor2.getCount() ; i++){
                cursor1.moveToNext();
                company1 = cursor1.getString(cursor1.getColumnIndex(Organization.COMPANY));
                title1 = cursor1.getString(cursor1.getColumnIndex(Organization.TITLE));
                type1 = cursor1.getInt(cursor1.getColumnIndex(Organization.TYPE));
                cursor2.moveToNext();
                company2 = cursor2.getString(cursor2.getColumnIndex(Organization.COMPANY));
                title2 = cursor2.getString(cursor2.getColumnIndex(Organization.TITLE));
                type2 = cursor2.getInt(cursor2.getColumnIndex(Organization.TYPE));
                Log.d("^^", "company1:" + company1 +" title1:" + title1 +" type1:" + type1 + " company2:" + company2 + " title2:" + title2 + " type2:" + type2);
                if(type2 == type1){
                    if((company2 == null && company1 == null) ||(company2 != null && company2.equals(company1))){
                        if((title2 == null && title1 == null) ||(title2 != null && title2.equals(title1))){
                            if(i == cursor2.getCount() -1){
                                try {
                                    cursor1.close();
                                    cursor2.close();
                                } catch (Exception e) {
                                    Log.d(TAG, "cursor is null when compareOrganization!");
                                }
                                Log.d("^^", "compare organization completed!");
                                compareIm(contactsIds);
                                return;
                            }
                        }else{
                            return;
                        }
                    }else{
                        return;
                    }
                }else{
                    return;
                }
            }
        }else{
            try {
                cursor1.close();
                cursor2.close();
            } catch (Exception e) {
                Log.d(TAG, "cursor is null when compareOrganization!");
            }
        }
        
    }
    private void compareIm(long[] contactsIds){
        String[] projection = new String[]{Im._ID, Im.DATA, Im.TYPE, Im.PROTOCOL};
        int type1 =0, type2 =0;
        String data1 =null, data2 =null;
        String protocol1 =null, protocol2 =null;
        
        
        Cursor cursor1 = mContentResolver.query(Data.CONTENT_URI, projection,
                Im.MIMETYPE + "=? AND " + Im.RAW_CONTACT_ID + "=?",
                new String[]{Im.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[0]))}, Im.TYPE);
        Cursor cursor2 = mContentResolver.query(Data.CONTENT_URI, projection,
                Im.MIMETYPE + "=? AND " + Im.RAW_CONTACT_ID + "=?",
                new String[]{Im.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[1]))}, Im.TYPE);
        
        if(cursor1.getCount() == cursor2.getCount()){
            if(cursor2.getCount() == 0){
                Log.d("^^", "compare im completed!");
                compareWebsite(contactsIds);
                return;
            }
            for (int i = 0; i < cursor2.getCount() ; i++){
                cursor1.moveToNext();
                data1 = cursor1.getString(cursor1.getColumnIndex(Im.DATA));
                protocol1 = cursor1.getString(cursor1.getColumnIndex(Im.PROTOCOL));
                type1 = cursor1.getInt(cursor1.getColumnIndex(Im.TYPE));
                cursor2.moveToNext();
                data2 = cursor2.getString(cursor2.getColumnIndex(Im.DATA));
                protocol2 = cursor2.getString(cursor2.getColumnIndex(Im.PROTOCOL));
                type2 = cursor2.getInt(cursor2.getColumnIndex(Im.TYPE));
                
                if(type2 == type1){
                    if((data2 == null && data1 == null) ||(data2 != null && data2.equals(data1))){
                        if((protocol2 == null && protocol1 == null) ||(protocol2 != null && protocol2.equals(protocol1))){
                            if(i == cursor2.getCount() -1){
                                try {
                                    cursor1.close();
                                    cursor2.close();
                                } catch (Exception e) {
                                    Log.d(TAG, "cursor is null when compareIm!");
                                }
                                Log.d("^^", "compare im completed!");
                                compareWebsite(contactsIds);
                                return;
                            }
                        }else{
                            return;
                        }
                    }else{
                        return;
                    }
                }else{
                    return;
                }
                  
                    
            }
        }else{
            try {
                cursor1.close();
                cursor2.close();
            } catch (Exception e) {
                Log.d(TAG, "cursor is null when compareIm!");
            }
        }
    }
    private void compareWebsite(long[] contactsIds){
        String[] projection = new String[]{Website._ID, Website.URL};
        String url1 =null, url2 =null;
        Cursor cursor1 = mContentResolver.query(Data.CONTENT_URI, projection,
                Website.MIMETYPE + "=? AND " + Website.RAW_CONTACT_ID + "=?",
                new String[]{Website.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[0]))}, Website.URL);
        Cursor cursor2 = mContentResolver.query(Data.CONTENT_URI, projection,
                Website.MIMETYPE + "=? AND " + Website.RAW_CONTACT_ID + "=?",
                new String[]{Website.CONTENT_ITEM_TYPE, String.valueOf(getRawContactId(contactsIds[1]))}, Website.URL);
        if(cursor1.getCount() == cursor2.getCount()){
            if(cursor2.getCount() == 0){
                Log.d("^^", "compare website completed!");
                addRepeatedContacts(contactsIds);
                return;
            }
            for (int i = 0; i < cursor2.getCount() ; i++){
                cursor1.moveToNext();
                url1 = cursor1.getString(cursor1.getColumnIndex(Website.URL));
                cursor2.moveToNext();
                url2 = cursor2.getString(cursor2.getColumnIndex(Website.URL));
                
                if((url2 == null && url1 == null) ||(url2 != null && url2.equals(url1))){
                    if(i == cursor2.getCount() -1){
                        try {
                            cursor1.close();
                            cursor2.close();
                        } catch (Exception e) {
                            Log.d(TAG, "cursor is null when compareWebsite!");
                        }
                        Log.d("^^", "compare website completed!");
                        addRepeatedContacts(contactsIds);
                        return;
                    }
                }else{
                    return;
                }
            }
        }else{
            try {
                cursor1.close();
                cursor2.close();
            } catch (Exception e) {
                Log.d(TAG, "cursor is null when compareWebsite!");
            }
        }
    }
    
    private void addRepeatedContacts(long[] contactIds){
        Log.d("^^", "add repeatedContact" + contactIds[1]);
        repeatedContactsUriList.add(Contacts.getLookupUri(contactIds[1], getLookupKey(contactIds[1])));
        uri = new Uri[repeatedContactsUriList.size()];
        repeatedContactsUriList.toArray(uri);
    }
    
    private long getRawContactId(long contactId) {
        long rawContactId = 0;
        Cursor cursor = mContentResolver.query(RawContacts.CONTENT_URI, new String[]{RawContacts._ID}, RawContacts.CONTACT_ID + "=" + contactId , null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                rawContactId = cursor.getLong(cursor.getColumnIndex(RawContacts._ID));
            }
            cursor.close();
        }
        return rawContactId;
    }
    
    private String getLookupKey(long contactId){
        String lookupKey = null;
        Cursor cursor = mContentResolver.query(Contacts.CONTENT_URI, new String[]{Contacts.LOOKUP_KEY}, Contacts._ID + "=" + contactId, null, null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                lookupKey = cursor.getString(cursor.getColumnIndex(Contacts.LOOKUP_KEY));
            }
        }
        return lookupKey;
    }
}
