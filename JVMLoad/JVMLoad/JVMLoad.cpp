// JVMLoad.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <windows.h>
#include <jni.h>

typedef jint (JNICALL CREATE_VM)       (JavaVM** vmPtr, void** env, void* initArgs);
typedef int (__stdcall FUNCCALL) (void);
int _tmain(int argc, _TCHAR* argv[])
{

//	TCHAR buf[1024];
//	::GetCurrentDirectory(1023, buf);

//	HMODULE l = ::LoadLibrary("loaderdll.dll");
	
//	FUNCCALL* f = (FUNCCALL *)::GetProcAddress(l,"fnloaderdll");
//	(*f)();



	char * jvmPath = "C:\\Program Files (x86)\\Java\\jre6\\bin\\client\\jvm.dll";
	HMODULE hMod = ::LoadLibrary(jvmPath);
	CREATE_VM* funcAddr = NULL;
	funcAddr = (CREATE_VM*)::GetProcAddress(hMod,"JNI_CreateJavaVM");
	
	const char* MIN_HEAP_OPTION = "JVMMinMemory";
    const char* MIN_HEAP_OPTION_PREFIX = "-Xms";

    const char* MAX_HEAP_OPTION = "JVMMaxMemory";
    const char* MAX_HEAP_OPTION_PREFIX = "-Xmx";

    const char* MIN_MEMORY_DEFAULT = "32m";
    const char* MAX_MEMORY_DEFAULT = "256m";

	JavaVMOption m_options[6];
	m_options[0].optionString = "-Djava.compiler=NONE";
	m_options[1].optionString = "-Djava.ext.dirs=.\\javalib";
	m_options[2].optionString = "-Djava.library.path=.\\";
	/* print JNI-related messages */
	m_options[3].optionString = "-verbose:jni";  

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
		jclass classid = env->FindClass("java/lang/String");
		jint q = 0;
		jthrowable exp = env->ExceptionOccurred();
		const jchar* cErrorMsg = NULL;
		if (exp) 
		{
			jclass eclass = env->GetObjectClass(exp);
			env->ExceptionDescribe();
			jmethodID mid = env->GetMethodID(eclass, "toString", "()Ljava/lang/String;");
			jstring jErrorMsg = (jstring) env->CallObjectMethod(exp, mid);
			cErrorMsg = env->GetStringChars(jErrorMsg, NULL);
			int k = 0;
		}
		
	}


	return 0;
}

