// utils.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <windows.h>
#include <string>
#include <map>
#include <vector>

#define MAX_KEY_LENGTH 255
#define MAX_VALUE_NAME 16383


std::string convertToUtf8(const wchar_t *pInput, int len)
{
	int utf8Len = WideCharToMultiByte(CP_UTF8,0,pInput,len,NULL,0,NULL,NULL);
	std::string strTo(utf8Len, 0);
	WideCharToMultiByte(CP_UTF8,0,pInput,len,&strTo[0],strTo.size(),NULL,NULL);
	return strTo;
}

std::wstring convertToWideChar(const char* pInput, int len)
{
	int wideCharLen = MultiByteToWideChar(CP_UTF8,0, pInput, len, NULL, 0);
	std::wstring strTo(wideCharLen, 0);
	MultiByteToWideChar(CP_UTF8,0, pInput, len, &strTo[0], wideCharLen);
	return strTo;
}

/**
* Reads the registry for DWORD which is treated as boolean. Returns false if any error occurs while
* dealing with the registry.
*/
bool readRegBool(std::wstring root, std::wstring keyName)
{
	HKEY hKey;
	::RegOpenKeyEx(HKEY_LOCAL_MACHINE, root.c_str(), 0, KEY_QUERY_VALUE|KEY_ENUMERATE_SUB_KEYS|KEY_WOW64_32KEY ,&hKey);

	DWORD value =  0;
	DWORD dwbufferSize(sizeof(DWORD));
	DWORD nResult = 0;
	DWORD err = ::RegQueryValueEx(hKey,keyName.c_str(),0,NULL,reinterpret_cast<LPBYTE>(&nResult), &dwbufferSize);
	bool retVal = false;
	if (err == ERROR_SUCCESS)
	{
		retVal = nResult;
	}
	::RegCloseKey(hKey);
	return retVal;

}

std::map<std::wstring, std::wstring> getRegistryKeyProperties(std::wstring keyName)
{
	HKEY hKey;
	::RegOpenKeyEx(HKEY_LOCAL_MACHINE, keyName.c_str(), 0, KEY_QUERY_VALUE|KEY_ENUMERATE_SUB_KEYS|KEY_WOW64_32KEY ,&hKey);
	TCHAR    achKey[MAX_KEY_LENGTH];   // buffer for subkey name
    DWORD    cbName;                   // size of name string 
    TCHAR    achClass[MAX_PATH] = TEXT("");  // buffer for class name 
    DWORD    cchClassName = MAX_PATH;  // size of class string 
    DWORD    cSubKeys=0;               // number of subkeys 
    DWORD    cbMaxSubKey;              // longest subkey size 
    DWORD    cchMaxClass;              // longest class string 
    DWORD    cValues;              // number of values for key 
    DWORD    cchMaxValue;          // longest value name 
    DWORD    cbMaxValueData;       // longest value data 
    DWORD    cbSecurityDescriptor; // size of security descriptor 
    FILETIME ftLastWriteTime;      // last write time 
 
    DWORD i, retCode; 
 
    TCHAR  achValue[MAX_VALUE_NAME]; 
    DWORD cchValue = MAX_VALUE_NAME; 
 
    // Query the top level key 
    retCode = RegQueryInfoKey(
        hKey,                    // key handle 
        achClass,                // buffer for class name 
        &cchClassName,           // size of class string 
        NULL,                    // reserved 
        &cSubKeys,               // number of subkeys 
        &cbMaxSubKey,            // longest subkey size 
        &cchMaxClass,            // longest class string 
        &cValues,                // number of values for this key 
        &cchMaxValue,            // longest value name 
        &cbMaxValueData,         // longest value data 
        &cbSecurityDescriptor,   // security descriptor 
        &ftLastWriteTime);       // last write time 
 
    
 
    // Enumerate the key values. 
	std::map<std::wstring, std::wstring> properties;
    if (cValues) 
    {
        printf( "\nNumber of values: %d\n", cValues);

        for (i=0, retCode=ERROR_SUCCESS; i<cValues; i++) 
        { 
            cchValue = MAX_VALUE_NAME; 
            achValue[0] = '\0'; 
			DWORD len = 4096;
			TCHAR * val = new TCHAR[2048];
			DWORD type = 0;
            retCode = RegEnumValue(hKey, i, 
                achValue, 
                &cchValue, 
                NULL, 
                &type,
                (LPBYTE)val,
				&len);			
 

            if (retCode == ERROR_SUCCESS ) 
            { 
				std::wstring name(L"");
				name.append(achValue);
				std::wstring value(L"");
				value.append(val);
				properties.insert(std::pair<std::wstring,std::wstring>(name,value));
            } 
        }
    }
	::RegCloseKey(hKey);
	return properties;
}

std::vector<std::wstring> getSubKeyNames(std::wstring baseKey)
{
	HKEY hKey;
	::RegOpenKeyEx(HKEY_LOCAL_MACHINE, baseKey.c_str(), 0, KEY_QUERY_VALUE|KEY_ENUMERATE_SUB_KEYS|KEY_WOW64_32KEY ,&hKey);
	TCHAR    achKey[MAX_KEY_LENGTH];   // buffer for subkey name
    DWORD    cbName;                   // size of name string 
    TCHAR    achClass[MAX_PATH] = TEXT("");  // buffer for class name 
    DWORD    cchClassName = MAX_PATH;  // size of class string 
    DWORD    cSubKeys=0;               // number of subkeys 
    DWORD    cbMaxSubKey;              // longest subkey size 
    DWORD    cchMaxClass;              // longest class string 
    DWORD    cValues;              // number of values for key 
    DWORD    cchMaxValue;          // longest value name 
    DWORD    cbMaxValueData;       // longest value data 
    DWORD    cbSecurityDescriptor; // size of security descriptor 
    FILETIME ftLastWriteTime;      // last write time 
 
    DWORD i, retCode; 
 
    TCHAR  achValue[MAX_VALUE_NAME]; 
    DWORD cchValue = MAX_VALUE_NAME; 
 
    // Query the top level key 
    retCode = RegQueryInfoKey(
        hKey,                    // key handle 
        achClass,                // buffer for class name 
        &cchClassName,           // size of class string 
        NULL,                    // reserved 
        &cSubKeys,               // number of subkeys 
        &cbMaxSubKey,            // longest subkey size 
        &cchMaxClass,            // longest class string 
        &cValues,                // number of values for this key 
        &cchMaxValue,            // longest value name 
        &cbMaxValueData,         // longest value data 
        &cbSecurityDescriptor,   // security descriptor 
        &ftLastWriteTime);       // last write time 
 
    
	// scan the subkeys to get the list of plugins
	std::vector<std::wstring> subKeyList;
    if (cSubKeys)
    {
        for (i=0; i<cSubKeys; i++) 
        { 
            cbName = MAX_KEY_LENGTH;
            retCode = RegEnumKeyEx(hKey, i,
                     achKey, 
                     &cbName, 
                     NULL, 
                     NULL, 
                     NULL, 
                     &ftLastWriteTime); 
            if (retCode == ERROR_SUCCESS) 
            {
				std::wstring basePath;
				basePath.append(achKey);
				subKeyList.push_back(basePath);
            }
        }
    }
	::RegCloseKey(hKey);
	return subKeyList;
}


int _tmain(int argc, _TCHAR* argv[])
{
	return 0;
}

