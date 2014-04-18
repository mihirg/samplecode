// loaderdll.cpp : Defines the entry point for the DLL application.
//

#include "stdafx.h"
#include "loaderdll.h"
#include <windows.h>
#include <jni.h>
#include <string>
#include <iostream>

typedef jint (JNICALL CREATE_VM)       (JavaVM** vmPtr, void** env, void* initArgs);
typedef jint (JNICALL GETDEF_VMARGS)   (void *args);

BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
	switch (ul_reason_for_call)
	{
	case DLL_PROCESS_ATTACH:
	case DLL_THREAD_ATTACH:
	case DLL_THREAD_DETACH:
	case DLL_PROCESS_DETACH:
		break;
	}
    return TRUE;
}

// This is an example of an exported variable
LOADERDLL_API int nloaderdll=0;

static std::string jniErrors;

static jint JNICALL my_vfprintf(FILE *fp, const char *format, va_list args) 
{     
	char buf[1024];
	vsnprintf(buf, sizeof(buf), format, args);
	::OutputDebugStringA(buf);
	std::cout << buf;
	jniErrors += buf;
	return 0;
} 

// This is an example of an exported function.
extern "C" int __declspec(dllexport) fnloaderdll(void)
{
	char * jvmPath = "C:\\Program Files (x86)\\Java\\jre6\\bin\\client\\jvm.dll";
	HMODULE hMod = ::LoadLibrary(jvmPath);
	CREATE_VM* funcAddr = NULL;
	funcAddr = (CREATE_VM*)::GetProcAddress(hMod,"JNI_CreateJavaVM");
	
	const char* MIN_HEAP_OPTION = "JVMMinMemory";
    const char* MIN_HEAP_OPTION_PREFIX = "-Xms";

    const char* MAX_HEAP_OPTION = "JVMMaxMemory";
    const char* MAX_HEAP_OPTION_PREFIX = "-Xmx";

    const char* MIN_MEMORY_DEFAULT = "32K";
    const char* MAX_MEMORY_DEFAULT = "1024M";

	JavaVMOption m_options[6];
	m_options[0].optionString = "-Djava.compiler=NONE";
	m_options[1].optionString = "-Djava.ext.dirs=.\\javalib\\";
	m_options[2].optionString = "-Djava.library.path=.\\";

	/* print JNI-related messages */
	m_options[3].optionString = "-verbose:class";  

	m_options[4].optionString="vfprintf";
	m_options[4].extraInfo = my_vfprintf;
	//m_options[5].optionString = "-Xdebug -Xrunjdwp:transport=dt_socket,address=8993";
	m_options[5].optionString = "-Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=n";

/*
	int len = 0;
	len = (int) strlen(MIN_MEMORY_DEFAULT);
	char* pMinMem = new char[strlen(MIN_HEAP_OPTION_PREFIX) + len + 1];
	sprintf(pMinMem,"%s%s",MIN_HEAP_OPTION_PREFIX, MIN_MEMORY_DEFAULT);
	m_options[4].optionString = pMinMem;

    len = 0;
    len = (int) strlen(MAX_MEMORY_DEFAULT);                        
    char* pMaxMem = new char[strlen(MAX_HEAP_OPTION_PREFIX) + len + 1];
    sprintf(pMaxMem,"%s%s",MAX_HEAP_OPTION_PREFIX,MAX_MEMORY_DEFAULT);
    m_options[5].optionString = pMaxMem;
*/

	JavaVMInitArgs vargs;
	memset(&vargs,0,sizeof(vargs));
	vargs.ignoreUnrecognized = JNI_TRUE;
	vargs.nOptions = 6;
	vargs.options = m_options;
	vargs.version = JNI_VERSION_1_4;
	JNIEnv* env = NULL;
	JavaVM* jvm = NULL;

	int ret = (*funcAddr)(&jvm, (void **)&env, &vargs);
	if (ret == 0)
	{
		//jclass classid = env->FindClass("in/gore/Main");
		jclass classid = env->FindClass("org/eclipse/swt/SWTError");
		jmethodID methodid = env->GetStaticMethodID(classid, "someMethod", "(I)V");
		jint q = 0;
		env->CallStaticVoidMethod(classid,methodid,q);
		jthrowable exp = env->ExceptionOccurred();
		if (exp)
			env->ExceptionDescribe();
	}

	return 42;
}

// This is the constructor of a class that has been exported.
// see loaderdll.h for the class definition
Cloaderdll::Cloaderdll()
{ 
	return; 
}
