#include "stdafx.h"

constexpr LPCWSTR GIT_CMD = L"git";

typedef HRESULT(STDAPICALLTYPE* TWslLaunchInteractive)(PCWSTR distroName, PCWSTR command, BOOL useCurrentWorkingDirectory, DWORD *pExitCode);

class TWslApi {

	private:
		HINSTANCE wslapi_dll;
		TWslLaunchInteractive WslLaunchInteractive;

	public:
		TWslApi();
		~TWslApi() noexcept;

		HRESULT LaunchInteractive(PCWSTR distroName, PCWSTR command, BOOL useCurrentWorkingDirectory, DWORD *pExitCode) noexcept;
};

TWslApi::TWslApi() {
	wslapi_dll = LoadLibrary(_T("wslapi.dll"));
	if (wslapi_dll == NULL) {
		throw GetLastError();
	}

	WslLaunchInteractive = reinterpret_cast<TWslLaunchInteractive>(GetProcAddress(wslapi_dll, "WslLaunchInteractive"));
	if (WslLaunchInteractive == NULL) {
		throw GetLastError();
	}
}

TWslApi::~TWslApi() {
	FreeLibrary(wslapi_dll);
}

HRESULT TWslApi::LaunchInteractive(PCWSTR distroName, PCWSTR command, BOOL useCurrentWorkingDirectory, DWORD *pExitCode) noexcept {
	return WslLaunchInteractive(distroName, command, useCurrentWorkingDirectory, pExitCode);
}

static LPWSTR GetRegistryValueWithMemAllocate(HKEY hKey, LPCWSTR lpSubKey, LPCWSTR lpValue, DWORD dwFlags) {
	LPWSTR value;
	DWORD value_sz;
	LSTATUS ret;

	ret = RegGetValue(hKey, lpSubKey, lpValue, dwFlags, NULL, NULL, &value_sz);
	if (ret != ERROR_SUCCESS) {
		return nullptr;
	}

	value = new TCHAR[value_sz];
	ret = RegGetValue(hKey, lpSubKey, lpValue, dwFlags, NULL, value, &value_sz);
	if (ret != ERROR_SUCCESS) {
		delete[] value;
		return nullptr;
	}

	return value;
}

static LPWSTR GetDefaultDistroName() {
	std::wstring subkey = L"Software\\Microsoft\\Windows\\CurrentVersion\\Lxss";
	LPWSTR value;

	value = GetRegistryValueWithMemAllocate(HKEY_CURRENT_USER, subkey.c_str(), L"DefaultDistribution", RRF_RT_REG_SZ);
	if (value == nullptr) {
		return nullptr;
	}

	subkey += L'\\';
	subkey += value;
	delete[] value;

	return GetRegistryValueWithMemAllocate(HKEY_CURRENT_USER, subkey.c_str(), L"DistributionName", RRF_RT_REG_SZ);
}

int wmain(int argc, LPWSTR argv[])
{
	TWslApi wslapi;

	LPWSTR distroName = GetDefaultDistroName();
	if (distroName == nullptr) {
		std::cerr << "Could not get default WSL distribution name" << std::endl;
		return -1;
	}

	std::wstring command = GIT_CMD;
	for (int idx = 1; idx < argc; idx++) {
		command += L" \"";
		command += argv[idx];
		command += L'"';
	}

	DWORD exit_code;
	HRESULT result = wslapi.LaunchInteractive(distroName, command.c_str(), TRUE, &exit_code);
	if (result != S_OK) {
		std::cerr << "WSL error (HRESULT: " << std::hex << std::showbase << result << ")" << std::endl;
		exit_code = result;
	}

	delete[] distroName;

	return exit_code;
}

