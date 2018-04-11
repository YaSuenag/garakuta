#include "stdafx.h"

#define DISTRO L"Ubuntu"
#define GIT_CMD _T("git")

typedef HRESULT(STDAPICALLTYPE* TWslLaunchInteractive)(PCWSTR distroName, PCWSTR command, BOOL useCurrentWorkingDirectory, DWORD *pExitCode);


int _tmain(int argc, TCHAR *argv[])
{
	HINSTANCE dll = LoadLibrary(_T("wslapi.dll"));
	if (dll == NULL) {
		std::cerr << "Could not load wslapi.dll" << std::endl;
		return -1;
	}

	auto WslLaunchInteractive = (TWslLaunchInteractive)GetProcAddress(dll, "WslLaunchInteractive");
	if (WslLaunchInteractive == NULL) {
		std::cerr << "Could not get address of WslLaunchInteractive from wsl.dll" << std::endl;
		return -2;
	}

	std::wstring command = GIT_CMD;
	for (int idx = 1; idx < argc; idx++) {
		command += L" \"";

#ifdef _UNICODE
		command += argv[idx];
#else
		int arglen = strlen(argv[idx]) + 1;
		PWSTR wstr = new WCHAR[arglen];
		mbstowcs(wstr, argv[idx], arglen);
		command += wstr;
		delete[] wstr;
#endif

		command += L"\"";
	}

	DWORD exit_code;
	WslLaunchInteractive(DISTRO, command.c_str(), TRUE, &exit_code);
	FreeLibrary(dll);

	return exit_code;
}

