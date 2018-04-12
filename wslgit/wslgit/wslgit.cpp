#include "stdafx.h"

#define GIT_CMD _T("git")

typedef HRESULT(STDAPICALLTYPE* TWslLaunchInteractive)(PCWSTR distroName, PCWSTR command, BOOL useCurrentWorkingDirectory, DWORD *pExitCode);


static LPTSTR GetDefaultDistroName() {
	std::basic_string<TCHAR> subkey = _T("Software\\Microsoft\\Windows\\CurrentVersion\\Lxss");
	DWORD value_sz;
	LPTSTR value;
	LONG result;

	result = RegGetValue(HKEY_CURRENT_USER, subkey.c_str(), _T("DefaultDistribution"), RRF_RT_REG_SZ, NULL, NULL, &value_sz);
	if (result != ERROR_SUCCESS) {
		return NULL;
	}
	value = new TCHAR[value_sz];
	RegGetValue(HKEY_CURRENT_USER, subkey.c_str(), _T("DefaultDistribution"), RRF_RT_REG_SZ, NULL, value, &value_sz);
	subkey += _T("\\");
	subkey += value;
	delete[] value;

	result = RegGetValue(HKEY_CURRENT_USER, subkey.c_str(), _T("DistributionName"), RRF_RT_REG_SZ, NULL, NULL, &value_sz);
	if (result != ERROR_SUCCESS) {
		return NULL;
	}
	value = new TCHAR[value_sz];
	RegGetValue(HKEY_CURRENT_USER, subkey.c_str(), _T("DistributionName"), RRF_RT_REG_SZ, NULL, value, &value_sz);

	return value;
}

int _tmain(int argc, TCHAR *argv[])
{
	LPTSTR distroName = GetDefaultDistroName();
	if (distroName == NULL) {
		std::cerr << "Could not get default WSL distribution name" << std::endl;
		return -1;
	}

	HINSTANCE dll = LoadLibrary(_T("wslapi.dll"));
	if (dll == NULL) {
		std::cerr << "Could not load wslapi.dll" << std::endl;
		delete[] distroName;
		return -2;
	}

	auto WslLaunchInteractive = (TWslLaunchInteractive)GetProcAddress(dll, "WslLaunchInteractive");
	if (WslLaunchInteractive == NULL) {
		std::cerr << "Could not get address of WslLaunchInteractive from wsl.dll" << std::endl;
		delete[] distroName;
		return -3;
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
	HRESULT result = WslLaunchInteractive(distroName, command.c_str(), TRUE, &exit_code);
	if (result != S_OK) {
		std::cout << "WSL error (HRESULT: " << std::hex << std::showbase << result << ")" << std::endl;
		exit_code = result;
	}

	FreeLibrary(dll);
	delete[] distroName;

	return exit_code;
}

