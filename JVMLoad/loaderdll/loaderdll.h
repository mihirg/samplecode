// The following ifdef block is the standard way of creating macros which make exporting 
// from a DLL simpler. All files within this DLL are compiled with the LOADERDLL_EXPORTS
// symbol defined on the command line. this symbol should not be defined on any project
// that uses this DLL. This way any other project whose source files include this file see 
// LOADERDLL_API functions as being imported from a DLL, whereas this DLL sees symbols
// defined with this macro as being exported.
#ifdef LOADERDLL_EXPORTS
#define LOADERDLL_API __declspec(dllexport)
#else
#define LOADERDLL_API __declspec(dllimport)
#endif

// This class is exported from the loaderdll.dll
class LOADERDLL_API Cloaderdll {
public:
	Cloaderdll(void);
	// TODO: add your methods here.
};

extern LOADERDLL_API int nloaderdll;

//LOADERDLL_API int fnloaderdll(void);
