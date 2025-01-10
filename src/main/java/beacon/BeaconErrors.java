package beacon;

import common.CommonUtils;

public class BeaconErrors {
    public static String toString(int n1, int n2, int n3, String string) {
        switch (n1) {
            case 0:
                return "DEBUG: " + string;
            case 1:
                return "Failed to get token";
            case 2:
                return "BypassUAC is for Windows 7 and later";
            case 3:
                return "You're already an admin";
            case 4:
                return "could not connect to pipe";
            case 5:
                return "Maximum links reached. Disconnect one";
            case 6:
                return "I'm already in SMB mode";
            case 7:
                return "could not run command (w/ token) because of its length of " + n2 + " bytes!";
            case 8:
                return "could not upload file: " + n2;
            case 9:
                return "could not get file time: " + n2;
            case 10:
                return "could not set file time: " + n2;
            case 11:
                return "Could not create service: " + n2;
            case 12:
                return "Failed to impersonate token: " + n2;
            case 13:
                return "copy failed: " + n2;
            case 14:
                return "move failed: " + n2;
            case 15:
                return "ppid " + n2 + " is in a different desktop session (spawned jobs may fail). Use 'ppid' to reset.";
            case 16:
                return "could not write to process memory: " + n2;
            case 17:
                return "could not adjust permissions in process: " + n2;
            case 18:
                return n2 + " is an x64 process (can't inject x86 content)";
            case 19:
                return n2 + " is an x86 process (can't inject x64 content)";
            case 20:
                return "Could not connect to pipe: " + n2;
            case 21:
                return "Could not bind to " + n2;
            case 22:
                return "Command length (" + n2 + ") too long";
            case 23:
                return "could not create pipe: " + n2;
            case 24:
                return "Could not create token: " + n2;
            case 25:
                return "Failed to impersonate token: " + n2;
            case 26:
                return "Could not start service: " + n2;
            case 27:
                return "Could not set PPID to " + n2;
            case 28:
                return "kerberos ticket purge failed: " + n2;
            case 29:
                return "kerberos ticket use failed: " + n2;
            case 30:
                return "Could not open process token: " + n2 + " (" + n3 + ")";
            case 31:
                return "could not allocate " + n2 + " bytes in process: " + n3;
            case 32:
                return "could not create remote thread in " + n2 + ": " + n3;
            case 33:
                return "could not open process " + n2 + ": " + n3;
            case 34:
                return "Could not set PPID to " + n2 + ": " + n3;
            case 35:
                return "Could not kill " + n2 + ": " + n3;
            case 36:
                return "Could not open process token: " + n2 + " (" + n3 + ")";
            case 37:
                return "Failed to impersonate token from " + n2 + " (" + n3 + ")";
            case 38:
                return "Failed to duplicate primary token for " + n2 + " (" + n3 + ")";
            case 39:
                return "Failed to impersonate logged on user " + n2 + " (" + n3 + ")";
            case 40:
                return "Could not open '" + string + "'";
            case 41:
                return "could not spawn " + string + " (token): " + n2;
            case 48:
                return "could not spawn " + string + ": " + n2;
            case 49:
                return "could not open " + string + ": " + n2;
            case 50:
                return "Could not connect to pipe (" + string + "): " + n2;
            case 51:
                return "Could not open service control manager on " + string + ": " + n2;
            case 52:
                return "could not open " + string + ": " + n2;
            case 53:
                return "could not run " + string;
            case 54:
                return "Could not create service " + string;
            case 55:
                return "Could not start service " + string;
            case 56:
                return "Could not query service " + string;
            case 57:
                return "Could not delete service " + string;
            case 58:
                return "Privilege '" + string + "' does not exist";
            case 59:
                return "Could not open process token";
            case 60:
                return "File '" + string + "' is either too large (>4GB) or size check failed";
            case 61:
                return "Could not determine full path of '" + string + "'";
            case 62:
                return "Can only LoadLibrary() in same-arch process";
            case 63:
                return "Could not open registry key: " + n2;
            case 64:
                return "x86 Beacon cannot adjust arguments in x64 process";
            case 65:
                return "Could not adjust arguments in process: " + n2;
            case 66:
                return "Real arguments are longer than fake arguments.";
            case 67:
                return "x64 Beacon cannot adjust arguments in x86 process";
            case 68:
                return "Could not connect to target";
            case 69:
                return "could not spawn " + string + " (token&creds): " + n2;
            case 70:
                return "Could not connect to target (stager)";
            case 71:
                return "Could not update process attribute: " + n2;
            case 72:
                return "could not create remote thread in " + n2 + ": " + n3;
            case 73:
                return "allocate section and copy data failed: " + n2;
            case 74:
                return "could not spawn " + string + " (token) with extended startup information. Reset ppid, disable blockdlls, or rev2self to drop your token.";
            case 75:
                return "current process will not auto-elevate COM object. Try from a program that lives in c:\\windows\\*";
        }
        CommonUtils.print_error("Unknown error toString(" + n1 + ", " + n2 + ", " + n3 + ", '" + string + "') BEACON_ERROR");
        return "Unknown error: " + n1;
    }
}
