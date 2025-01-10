package c2profile;

import c2profile.Checkers;
import c2profile.Profile;
import common.CommonUtils;
import common.RegexParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import sleep.error.SyntaxError;
import sleep.error.YourCodeSucksException;
import sleep.parser.LexicalAnalyzer;
import sleep.parser.Parser;
import sleep.parser.ParserUtilities;
import sleep.parser.StringIterator;
import sleep.parser.Token;
import sleep.parser.TokenList;

public class Loader {
    protected String code;

    protected Profile result;

    protected String loadme;

    protected ProfileParser parser;

    protected Set options = new HashSet();

    protected Set indicators = new HashSet();

    protected Set statementa = new HashSet();

    protected Set blocks = new HashSet();

    protected Set statementb = new HashSet();

    protected Set sealme = new HashSet();

    protected Set numbers = new HashSet();

    protected Set files = new HashSet();

    protected Set booleans = new HashSet();

    protected Set verbs = new HashSet();

    protected Set ips = new HashSet();

    protected Set dates = new HashSet();

    protected Set freepass = new HashSet();

    protected Set strings = new HashSet();

    protected Set disable = new HashSet();

    protected Set allocators = new HashSet();

    protected Set variants = new HashSet();

    protected Set specialh = new HashSet();

    protected File parent;

    public String find(String string) {
        File file = new File(string);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        file = new File(this.parent, string);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        return string;
        // return file.exists() ? file.getAbsolutePath() : string;
    }

    public Loader(String string1, String string2, Profile profile) {
        this.loadme = string1;
        this.code = string2;
        this.result = profile;
        this.parent = new File(string1).getParentFile();
        this.parser = new ProfileParser(string1);
        profile.addParameter(".amsi_disable", "false");
        profile.addParameter(".sleeptime", "60000");
        profile.addParameter(".jitter", "0");
        profile.addParameter(".maxdns", "255");
        profile.addParameter(".useragent", "<RAND>");
        profile.addParameter(".spawnto", "rundll32.exe");
        profile.addParameter(".spawnto_x86", "%windir%\\syswow64\\rundll32.exe");
        profile.addParameter(".spawnto_x64", "%windir%\\sysnative\\rundll32.exe");
        profile.addParameter(".pipename", "msagent_##");
        profile.addParameter(".pipename_stager", "status_##");
        profile.addParameter(".sample_name",
                CommonUtils.strrep(new File(string1).getName(), ".profile", ""));
        profile.addParameter(".dns_idle", "0.0.0.0");
        profile.addParameter(".dns_sleep", "0");
        profile.addParameter(".dns_stager_subhost", "");
        profile.addParameter(".dns_stager_prepend", "");
        profile.addParameter(".dns_max_txt", "252");
        profile.addParameter(".dns_ttl", "1");
        profile.addParameter(".tcp_port", "4444");
        profile.addParameter(".host_stage", "true");
        profile.addParameter(".https-certificate.CN", "");
        profile.addParameter(".https-certificate.OU", "");
        profile.addParameter(".https-certificate.O", "");
        profile.addParameter(".https-certificate.L", "");
        profile.addParameter(".https-certificate.ST", "");
        profile.addParameter(".https-certificate.C", "");
        profile.addParameter(".https-certificate.validity", "3650");
        profile.addParameter(".https-certificate.keystore", "");
        profile.addParameter(".https-certificate.password", "123456");
        profile.addParameter(".http-get.verb", "GET");
        profile.addParameter(".http-post.verb", "POST");
        profile.addParameter(".code-signer.digest_algorithm", "SHA256");
        profile.addParameter(".code-signer.timestamp", "false");
        profile.addParameter(".code-signer.timestamp_mode", "AUTHENTICODE");
        profile.addParameter(".code-signer.keystore", "");
        profile.addParameter(".code-signer.password", "");
        profile.addParameter(".code-signer.alias", "");
        profile.addParameter(".code-signer.program_name", "");
        profile.addParameter(".code-signer.program_url", "");
        profile.addParameter(".code-signer.timestamp_url", "");
        profile.addParameter(".stage.checksum", "0");
        profile.addParameter(".stage.cleanup", "false");
        profile.addParameter(".stage.compile_time", "");
        profile.addParameter(".stage.entry_point", "-1");
        profile.addParameter(".stage.name", "");
        profile.addParameter(".stage.module_x86", "");
        profile.addParameter(".stage.module_x64", "");
        profile.addParameter(".stage.image_size_x86", "0");
        profile.addParameter(".stage.image_size_x64", "0");
        profile.addParameter(".stage.obfuscate", "false");
        profile.addParameter(".stage.sleep_mask", "false");
        profile.addParameter(".stage.userwx", "true");
        profile.addParameter(".stage.stomppe", "true");
        profile.addParameter(".stage.rich_header", "<DEFAULT>");
        profile.addParameter(".post-ex.spawnto_x86", "%windir%\\syswow64\\rundll32.exe");
        profile.addParameter(".post-ex.spawnto_x64", "%windir%\\sysnative\\rundll32.exe");
        profile.addParameter(".post-ex.obfuscate", "false");
        profile.addParameter(".post-ex.smartinject", "false");
        profile.addParameter(".post-ex.amsi_disable", "false");
        profile.addParameter(".process-inject.min_alloc", "0");
        profile.addParameter(".process-inject.startrwx", "true");
        profile.addParameter(".process-inject.userwx", "true");
        profile.addParameter(".process-inject.CreateRemoteThread", "true");
        profile.addParameter(".process-inject.RtlCreateUserThread", "true");
        profile.addParameter(".process-inject.SetThreadContext", "true");
        profile.addParameter(".process-inject.allocator", "VirtualAllocEx");
        profile.addParameter(".create_remote_thread", "true");
        profile.addParameter(".hijack_remote_thread", "true");
        profile.addParameter(".http-stager.uri_x86", "");
        profile.addParameter(".http-stager.uri_x64", "");
        profile.addParameter(".http-config.trust_x_forwarded_for", "false");
        profile.addParameter(".http-config.headers", "");
        profile.addList(".process-inject.execute");
        profile.addParameter(".bind_tcp_garbage",
                CommonUtils.bString(CommonUtils.randomData(CommonUtils.rand(1024))));
        this.freepass.add(".http-stager.server.output");
        this.options.add(".amsi_disable");
        this.options.add(".sleeptime");
        this.options.add(".jitter");
        this.options.add(".maxdns");
        this.options.add(".http-get.uri");
        this.options.add(".http-post.uri");
        this.options.add(".http-get.verb");
        this.options.add(".http-post.verb");
        this.options.add(".useragent");
        this.options.add(".spawnto");
        this.options.add(".spawnto_x86");
        this.options.add(".spawnto_x64");
        this.options.add(".pipename");
        this.options.add(".pipename_stager");
        this.options.add(".dns_idle");
        this.options.add(".dns_sleep");
        this.options.add(".host_stage");
        this.options.add(".dns_stager_prepend");
        this.options.add(".dns_stager_subhost");
        this.options.add(".create_remote_thread");
        this.options.add(".hijack_remote_thread");
        this.options.add(".dns_max_txt");
        this.options.add(".dns_ttl");
        this.options.add(".sample_name");
        this.options.add(".tcp_port");
        this.options.add(".post-ex.spawnto_x86");
        this.options.add(".post-ex.spawnto_x64");
        this.options.add(".post-ex.obfuscate");
        this.options.add(".post-ex.amsi_disable");
        this.options.add(".post-ex.smartinject");
        this.options.add(".stage.userwx");
        this.options.add(".stage.compile_time");
        this.options.add(".stage.checksum");
        this.options.add(".stage.cleanup");
        this.options.add(".stage.entry_point");
        this.options.add(".stage.name");
        this.options.add(".stage.obfuscate");
        this.options.add(".stage.sleep_mask");
        this.options.add(".stage.stomppe");
        this.options.add(".stage.image_size_x86");
        this.options.add(".stage.image_size_x64");
        this.options.add(".stage.module_x86");
        this.options.add(".stage.module_x64");
        this.options.add(".stage.rich_header");
        this.options.add(".process-inject.min_alloc");
        this.options.add(".process-inject.startrwx");
        this.options.add(".process-inject.userwx");
        this.options.add(".process-inject.allocator");
        this.strings.add(".stage.name");
        this.options.add(".https-certificate.CN");
        this.options.add(".https-certificate.OU");
        this.options.add(".https-certificate.O");
        this.options.add(".https-certificate.L");
        this.options.add(".https-certificate.ST");
        this.options.add(".https-certificate.C");
        this.options.add(".https-certificate.validity");
        this.options.add(".https-certificate.keystore");
        this.options.add(".https-certificate.password");
        this.options.add(".code-signer.keystore");
        this.options.add(".code-signer.password");
        this.options.add(".code-signer.alias");
        this.options.add(".code-signer.program_name");
        this.options.add(".code-signer.program_url");
        this.options.add(".code-signer.timestamp_url");
        this.options.add(".code-signer.timestamp_mode");
        this.options.add(".code-signer.timestamp");
        this.options.add(".code-signer.digest_algorithm");
        this.options.add(".http-stager.uri_x86");
        this.options.add(".http-stager.uri_x64");
        this.options.add(".http-config.headers");
        this.options.add(".http-config.trust_x_forwarded_for");
        this.numbers.add(".sleeptime");
        this.numbers.add(".jitter");
        this.numbers.add(".maxdns");
        this.numbers.add(".dns_sleep");
        this.numbers.add(".https-certificate.validity");
        this.numbers.add(".stage.entry_point");
        this.numbers.add(".stage.image_size_x86");
        this.numbers.add(".stage.image_size_x64");
        this.numbers.add(".dns_max_txt");
        this.numbers.add(".dns_ttl");
        this.numbers.add(".process-inject.min_alloc");
        this.numbers.add(".tcp_port");
        this.booleans.add(".host_http_stager");
        this.booleans.add(".code-signer.timestamp");
        this.booleans.add(".stage.userwx");
        this.booleans.add(".create_remote_thread");
        this.booleans.add(".hijack_remote_thread");
        this.booleans.add(".stage.obfuscate");
        this.booleans.add(".stage.sleep_mask");
        this.booleans.add(".stage.stomppe");
        this.booleans.add(".stage.cleanup");
        this.booleans.add(".process-inject.startrwx");
        this.booleans.add(".process-inject.userwx");
        this.booleans.add(".amsi_disable");
        this.booleans.add(".post-ex.obfuscate");
        this.booleans.add(".post-ex.amsi_disable");
        this.booleans.add(".post-ex.smartinject");
        this.booleans.add(".http-config.trust_x_forwarded_for");
        this.files.add(".https-certificate.keystore");
        this.files.add(".code-signer.keystore");
        this.verbs.add(".http-get.verb");
        this.verbs.add(".http-post.verb");
        this.allocators.add(".process-inject.allocator");
        this.ips.add(".dns_idle");
        this.dates.add(".stage.compile_time");
        this.disable.add(".process-inject.CreateRemoteThread");
        this.disable.add(".process-inject.SetThreadContext");
        this.disable.add(".process-inject.RtlCreateUserThread");
        this.indicators.add(".http-get.server.header");
        this.indicators.add(".http-get.client.header");
        this.indicators.add(".http-get.client.parameter");
        this.indicators.add(".http-post.server.header");
        this.indicators.add(".http-post.client.header");
        this.indicators.add(".http-post.client.parameter");
        this.indicators.add(".http-stager.client.parameter");
        this.indicators.add(".http-stager.client.header");
        this.indicators.add(".http-stager.server.header");
        this.indicators.add(".stage.transform-x86.strrep");
        this.indicators.add(".stage.transform-x64.strrep");
        this.indicators.add(".http-config.header");
        this.variants.add(".http-stager");
        this.variants.add(".http-get");
        this.variants.add(".http-post");
        this.variants.add(".https-certificate");
        this.blocks.add(".http-get");
        this.blocks.add(".http-get.client");
        this.blocks.add(".http-get.client.metadata");
        this.blocks.add(".http-get.server");
        this.blocks.add(".http-get.server.output");
        this.blocks.add(".http-post");
        this.blocks.add(".http-post.client");
        this.blocks.add(".http-post.client.id");
        this.blocks.add(".http-post.client.output");
        this.blocks.add(".http-post.server");
        this.blocks.add(".http-post.server.output");
        this.blocks.add(".http-stager");
        this.blocks.add(".http-stager.client");
        this.blocks.add(".http-stager.server");
        this.blocks.add(".http-stager.server.output");
        this.blocks.add(".https-certificate");
        this.blocks.add(".code-signer");
        this.blocks.add(".stage");
        this.blocks.add(".stage.transform-x86");
        this.blocks.add(".stage.transform-x64");
        this.blocks.add(".process-inject");
        this.blocks.add(".process-inject.transform-x86");
        this.blocks.add(".process-inject.transform-x64");
        this.blocks.add(".http-config");
        this.blocks.add(".post-ex");
        this.blocks.add(".process-inject.execute");
        this.statementa.add(".stage.transform-x86.prepend");
        this.statementa.add(".stage.transform-x86.append");
        this.statementa.add(".stage.transform-x64.prepend");
        this.statementa.add(".stage.transform-x64.append");
        this.statementa.add(".process-inject.transform-x86.prepend");
        this.statementa.add(".process-inject.transform-x86.append");
        this.statementa.add(".process-inject.transform-x64.prepend");
        this.statementa.add(".process-inject.transform-x64.append");
        this.statementa.add(".http-stager.server.output.prepend");
        this.statementa.add(".http-stager.server.output.append");
        this.statementa.add(".stage.string");
        this.statementa.add(".stage.stringw");
        this.statementa.add(".stage.data");
        this.statementa.add(".process-inject.disable");
        this.sealme.add(".http-get.client.metadata");
        this.sealme.add(".http-get.server.output");
        this.sealme.add(".http-post.client.id");
        this.sealme.add(".http-post.client.output");
        this.sealme.add(".http-post.server.output");
        this.sealme.add(".http-stager.server.output");
        allowDTL(".http-get.client.metadata", this.statementb, this.statementa);
        allowDTL(".http-get.server.output", this.statementb, this.statementa);
        allowDTL(".http-post.client.id", this.statementb, this.statementa);
        allowDTL(".http-post.client.output", this.statementb, this.statementa);
        allowDTL(".http-post.server.output", this.statementb, this.statementa);
        this.statementa.add(".http-get.client.metadata.header");
        this.statementa.add(".http-get.client.metadata.parameter");
        this.statementa.add(".http-post.client.id.header");
        this.statementa.add(".http-post.client.id.parameter");
        this.statementa.add(".http-post.client.output.header");
        this.statementa.add(".http-post.client.output.parameter");
        this.statementb.add(".http-get.client.metadata.print");
        this.statementb.add(".http-get.server.output.print");
        this.statementb.add(".http-post.client.output.print");
        this.statementb.add(".http-post.client.id.print");
        this.statementb.add(".http-post.server.output.print");
        this.statementb.add(".http-stager.server.output.print");
        this.statementb.add(".process-inject.execute.CreateThread");
        this.statementb.add(".process-inject.execute.CreateRemoteThread");
        this.statementb.add(".process-inject.execute.SetThreadContext");
        this.statementb.add(".process-inject.execute.RtlCreateUserThread");
        this.statementb.add(".process-inject.execute.NtQueueApcThread");
        this.statementb.add(".process-inject.execute.NtQueueApcThread-s");
        this.statementa.add(".process-inject.execute.CreateThread");
        this.statementa.add(".process-inject.execute.CreateRemoteThread");
        this.statementb.add(".http-get.client.metadata.uri-append");
        this.statementb.add(".http-post.client.id.uri-append");
        this.statementb.add(".http-post.client.output.uri-append");
        this.specialh.add(".http-get.client");
        this.specialh.add(".http-post.client");
    }

    private void allowDTL(String string, Set set1, Set set2) {
        set1.add(string + ".base64");
        set1.add(string + ".base64url");
        set1.add(string + ".netbios");
        set1.add(string + ".netbiosu");
        set1.add(string + ".mask");
        set2.add(string + ".prepend");
        set2.add(string + ".append");
    }

    public void parse(String string) {
        parse(code, string, 1);
        Iterator iterator = sealme.iterator();
        while (iterator.hasNext()) {
            String str = iterator.next() + "";
            if (result.getProgram(str) == null && !freepass.contains(str)) {
                parser.reportError(
                        new SyntaxError("Profile is missing a mandatory program spec", str, 1));
            }
        }
        iterator = options.iterator();
        while (iterator.hasNext()) {
            String str = iterator.next() + "";
            if (!result.hasString(str)) {
                parser.reportError(
                        new SyntaxError("Profile is missing a mandatory option", str, 1));
            }
        }
        if (parser.hasErrors()) {
            parser.resolveErrors();
        }
    }

    public void parse(String string1, String string2, int n) {
        TokenList tokenList = LexicalAnalyzer.CreateTerms(parser,
                new StringIterator(string1, n));
        Token[] arrtoken = tokenList.getTokens();
        int i = 0;
        while (i < arrtoken.length) {
            i = parse(arrtoken, i, string2);
        }
    }

    private static String namespace(String string) {
        if ("".equals(string)) {
            return "<Global>";
        }
        return "<" + string + ">";
        // return "".equals(string) ? "<Global>" : ("<" + string + ">");
    }

    public String convert(String string, Token token) {
        StringBuffer stringBuffer = new StringBuffer();
        StringIterator stringIterator = new StringIterator(ParserUtilities.extract(string),
                token.getHint());
        while (stringIterator.hasNext()) {
            char c = stringIterator.next();
            if (c == '\\' && stringIterator.hasNext()) {
                c = stringIterator.next();
                if (c == 'u') {
                    if (!stringIterator.hasNext(4)) {
                        this.parser.reportErrorWithMarker("not enough remaining characters for \\uXXXX", stringIterator.getErrorToken());
                        continue;
                    }
                    String str = stringIterator.next(4);
                    try {
                        int i = Integer.parseInt(str, 16);
                        stringBuffer.append((char) i);
                        continue;
                    } catch (NumberFormatException numberFormatException) {
                        this.parser.reportErrorWithMarker("invalid unicode escape \\u" + str + " - must be hex digits", stringIterator.getErrorToken());
                        continue;
                    }
                }
                if (c == 'x') {
                    if (!stringIterator.hasNext(2)) {
                        this.parser.reportErrorWithMarker("not enough remaining characters for \\uXXXX", stringIterator.getErrorToken());
                        continue;
                    }
                    String str = stringIterator.next(2);
                    try {
                        int i = Integer.parseInt(str, 16);
                        stringBuffer.append((char) i);
                        continue;
                    } catch (NumberFormatException numberFormatException) {
                        this.parser.reportErrorWithMarker("invalid unicode escape \\x" + str + " - must be hex digits", stringIterator.getErrorToken());
                        continue;
                    }
                }
                if (c == 'n') {
                    stringBuffer.append("\n");
                    continue;
                }
                if (c == 'r') {
                    stringBuffer.append("\r");
                    continue;
                }
                if (c == 't') {
                    stringBuffer.append("\t");
                    continue;
                }
                if (c == '\\') {
                    stringBuffer.append("\\");
                    continue;
                }
                if (c == '"') {
                    stringBuffer.append("\"");
                    continue;
                }
                if (c == '\'') {
                    stringBuffer.append("'");
                    continue;
                }
                this.parser.reportErrorWithMarker("unknown escape \\" + c, stringIterator.getErrorToken());
                continue;
            }
            stringBuffer.append(c);
        }
        return stringBuffer.toString();
    }

    public int parse(Token[] arrtoken, int n, String string) {
        if (n + 3 < arrtoken.length) {
            String str1 = arrtoken[n].toString();
            String str2 = arrtoken[n + 1].toString();
            String str3 = arrtoken[n + 2].toString();
            String str4 = arrtoken[n + 3].toString();
            if (Checkers.isSetStatement(str1, str2, str3, str4)) {
                if (!options.contains(string + "." + str2)) {
                    parser.reportError("invalid option for " + namespace(string), arrtoken[n + 1]);
                } else {
                    String str = convert(str3, arrtoken[n + 2]);
                    if (numbers.contains(string + "." + str2)
                            && !Checkers.isNumber(str))
                        parser.reportError("option " + namespace(string + "." + str2) + " requires a number", arrtoken[n + 2]);
                    if (booleans.contains(string + "." + str2) && !Checkers.isBoolean(str))
                        parser.reportError("option " + namespace(string + "." + str2) + " requires true or false", arrtoken[n + 2]);
                    if (verbs.contains(string + "." + str2) && !Checkers.isHTTPVerb(str))
                        parser.reportError("option " + namespace(string + "." + str2) + " requires a valid HTTP verb", arrtoken[n + 2]);
                    if (ips.contains(string + "." + str2) && !CommonUtils.isIP(str))
                        parser.reportError("option " + namespace(string + "." + str2) + " requires an IPv4 address", arrtoken[n + 2]);
                    if (dates.contains(string + "." + str2) && !Checkers.isDate(str))
                        parser.reportError("option " + namespace(string + "." + str2) + " requires a 'dd MMM YYYY hh:mm:ss' date", arrtoken[n + 2]);
                    if (allocators.contains(string + "." + str2) && !Checkers.isAllocator(str))
                        parser.reportError("option " + namespace(string + "." + str2) + " requires VirtualAllocEx or NtMapViewOfSection", arrtoken[n + 2]);
                    if (files.contains(string + "." + str2)) {
                        String str5 = find(str);
                        if ((new File(str5)).exists()) {
                            result.addParameter(string + "." + str2, str5);
                        } else {
                            parser.reportError("could not find file in " + namespace(string + "." + str2), arrtoken[n + 2]);
                        }
                    } else if (strings.contains(string + "." + str2)) {
                        result.addToString(string, CommonUtils.toBytes(str + Character.MIN_VALUE));
                        result.addToString(string, CommonUtils.randomDataNoZeros(5));
                        result.addParameter(string + "." + str2, str);
                    } else {
                        result.addParameter(string + "." + str2, str);
                    }
                }
                return n + 4;
            }
            if (Checkers.isIndicator(str1, str2, str3, str4)) {
                String str5 = str1;
                String str6 = convert(str2, arrtoken[n + 1]);
                String str7 = convert(str3, arrtoken[n + 2]);
                if (!indicators.contains(string + "." + str5)) {
                    if ("strrep".equals(str5)) {
                        parser.reportError("invalid token for " + namespace(string), arrtoken[n]);
                    } else {
                        parser.reportError("invalid indicator for " + namespace(string), arrtoken[n]);
                    }
                } else if (specialh.contains(string) && str5.equals("header") && "host".equals(str6.toLowerCase())) {
                    result.addCommand(string, "!hostheader", str6 + ": " + str7);
                } else if (str5.equals("header")) {
                    result.addCommand(string, "!" + str5, str6 + ": " + str7);
                } else if (str5.equals("parameter")) {
                    result.addCommand(string, "!" + str5, str6 + "=" + str7);
                } else if (str5.equals("strrep")) {
                    if (str7.length() > str6.length()) {
                        parser.reportError("strrep length(original) < length(replacement value). I can't do this.", arrtoken[n + 2]);
                    } else {
                        while (str7.length() < str6.length())
                            str7 = str7 + Character.MIN_VALUE;
                        result.addCommand(string, str5, str6 + str7);
                    }
                }
                return n + 4;
            }
        }
        if (n + 2 < arrtoken.length) {
            String str1 = arrtoken[n].toString();
            String str2 = arrtoken[n + 1].toString();
            String str3 = arrtoken[n + 2].toString();
            if (Checkers.isStatementArgBlock(str1, str2, str3)) {
                if (!variants.contains(string + "." + str1)) {
                    parser.reportError("Variant block is not valid for " + namespace(string), arrtoken[n]);
                } else {
                    String str = convert(str2, arrtoken[n + 1]);
                    result.activateVariant(str);
                    parse(ParserUtilities.extract(str3), string + "." + str1, arrtoken[n + 2].getHint());
                    result.addCommand(string, "build", string + "." + str1);
                    if (sealme.contains(string + "." + str1) && !result.isSealed(string + "." + str1))
                        parser.reportError("Program " + namespace(string + "." + str1) + " must end with a termination statement", arrtoken[n + 2]);
                    result.activateVariant("default");
                }
                return n + 3;
            }
            if (Checkers.isStatementArg(str1, str2, str3)) {
                if (!statementa.contains(string + "." + str1)) {
                    parser.reportError("Statement with argument is not valid for " + namespace(string), arrtoken[n]);
                } else if (result.isSealed(string)) {
                    parser.reportError("Program is terminated. Can't add transform statements to " + namespace(string), arrtoken[n]);
                } else if (str1.equals("string")) {
                    String str = convert(str2, arrtoken[n + 1]) + Character.MIN_VALUE;
                    result.addToString(string, CommonUtils.toBytes(str));
                    result.logToString(string, str);
                } else if (str1.equals("stringw")) {
                    String str = convert(str2, arrtoken[n + 1]) + Character.MIN_VALUE;
                    result.addToString(string, CommonUtils.toBytes(str, "UTF-16LE"));
                    result.logToString(string, str);
                } else if (str1.equals("data")) {
                    String str = convert(str2, arrtoken[n + 1]);
                    result.addToString(string, CommonUtils.toBytes(str));
                } else if (str1.equals("disable")) {
                    String str = convert(str2, arrtoken[n + 1]);
                    if (!disable.contains(string + "." + str)) {
                        parser.reportError("function " + str + " is not a recognized disable option", arrtoken[n + 1]);
                    } else {
                        result.addParameter(string + "." + str, "false");
                    }
                } else if (".process-inject.execute".equals(string)) {
                    String str = convert(str2, arrtoken[n + 1]);
                    RegexParser regexParser = new RegexParser(str);
                    if (regexParser.matches("(.*?)!(.*?)\\+0x([a-fA-F0-9]+?)")) {
                        String str4 = regexParser.group(1);
                        String str5 = regexParser.group(2);
                        int i = CommonUtils.toNumberFromHex(regexParser.group(3), 2147483647);
                        if (i < 0 || i >= 65535) {
                            parser.reportError("function hint for " + str1 + " has invalid offset " + i + ". Allowed values 0 < offset < 0xffff", arrtoken[n + 1]);
                        } else {
                            result.addCommand(string, str1, str4 + " " + str5 + " " + i);
                        }
                    } else if (regexParser.matches("(.*?)!(.*?)\\+(.*?)")) {
                        parser.reportError("offset '" + regexParser.group(3) + "' for function hint " + str1 + " is not 0x#### format", arrtoken[n + 1]);
                    } else if (regexParser.matches("(.*?)!(.*?)")) {
                        String str4 = regexParser.group(1);
                        String str5 = regexParser.group(2);
                        result.addCommand(string, str1, str4 + " " + str5 + " 0");
                    } else {
                        parser.reportError("function hint for " + str1 + " is not module.dll!FunctionName+0x## or module.dll!FunctionName format", arrtoken[n + 1]);
                    }
                } else {
                    result.addCommand(string, str1, convert(str2, arrtoken[n + 1]));
                }
                return n + 3;
            }
        }
        if (n + 1 < arrtoken.length) {
            String str1 = arrtoken[n].toString();
            String str2 = arrtoken[n + 1].toString();
            if (Checkers.isStatementBlock(str1, str2)) {
                if (!blocks.contains(string + "." + str1)) {
                    parser.reportError("Block is not valid for " + namespace(string), arrtoken[n]);
                } else {
                    parse(ParserUtilities.extract(str2), string + "." + str1, arrtoken[n + 1].getHint());
                    result.addCommand(string, "build", string + "." + str1);
                    if (sealme.contains(string + "." + str1) && !result.isSealed(string + "." + str1))
                        parser.reportError("Program " + namespace(string + "." + str1) + " must end with a termination statement", arrtoken[n + 1]);
                }
                return n + 2;
            }
            if (Checkers.isStatement(str1, str2)) {
                if (!statementb.contains(string + "." + str1)) {
                    parser.reportError("Statement is not valid for " + namespace(string), arrtoken[n]);
                } else if (result.isSealed(string)) {
                    parser.reportError("Program is terminated. Can't add transform statements to " + namespace(string), arrtoken[n]);
                } else {
                    result.addCommand(string, str1, null);
                }
                return n + 2;
            }
        }
        if (n < arrtoken.length) {
            String str = arrtoken[n].toString();
            if (Checkers.isComment(str))
                return n + 1;
            parser.reportError("Unknown statement in " + namespace(string), arrtoken[n]);
            return 10000;
        }
        return 0;
    }

    public static Profile LoadDefaultProfile() {
        InputStream inputStream = Loader.class.getClassLoader().getResourceAsStream("resources/default.profile");
        return LoadProfile("default", inputStream);
    }

    public static Profile LoadProfile(String string) {
        try {
            File file = new File(string);
            return LoadProfile(string, new FileInputStream(file));
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public static Profile LoadProfile(String string, InputStream inputStream) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                stringBuffer.append(str);
                stringBuffer.append('\n');
            }
            bufferedReader.close();
            Profile profile = new Profile();
            Loader loader = new Loader(string, stringBuffer.toString(), profile);
            loader.parse("");
            if (loader.parser.hasErrors()) {
                return null;
            }
            return profile;
            // return loader.parser.hasErrors() ? null : profile;
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private static class ProfileParser extends Parser {
        public ProfileParser(String string) {
            super(string, "");
        }

        public void resolveErrors() throws YourCodeSucksException {
            if (hasErrors()) {
                CommonUtils.print_error("Error(s) while compiling " + name);
                errors.addAll(warnings);
                YourCodeSucksException yourCodeSucksException =
                        new YourCodeSucksException(errors);
                yourCodeSucksException.printErrors(System.out);
            }
        }
    }
}
