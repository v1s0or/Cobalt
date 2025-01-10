package aggressor.dialogs;

import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.MultiFrame;
import common.Callback;
import common.CommonUtils;
import common.TeamQueue;
import common.UploadFile;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class HostFileDialog implements DialogListener, Callback, UploadFile.UploadNotify {

    protected MultiFrame window;

    protected JFrame dialog = null;

    protected TeamQueue conn = null;

    protected DataManager datal = null;

    protected String file;

    protected String uri;

    protected String port;

    protected String mime;

    protected String host;

    protected String proto;

    protected boolean ssl;

    protected ActionEvent event;

    public HostFileDialog(MultiFrame paramMultiFrame, TeamQueue teamQueue, DataManager dataManager) {
        this.window = paramMultiFrame;
        this.conn = teamQueue;
        this.datal = dataManager;
    }

    public static String fixMime(String string) {
        String[] arrstring = {
                "application/octet-stream::deploy", "application/acad::dwg", "application/arj::arj", "application/astound::asn", "application/clariscad::ccad", "application/drafting::drw", "application/dxf::dxf", "application/hta::hta", "application/i-deas::unv", "application/iges::igs",
                "application/java-archive::jar", "application/mac-binhex40::hqx", "application/msaccess::mdb", "application/msexcel::xlw", "application/mspowerpoint::ppt", "application/msproject::mpp", "application/msword::w6w", "application/mswrite::wri", "application/octet-stream::bin", "application/oda::oda",
                "application/pdf::pdf", "application/postscript::ps", "application/pro_eng::prt", "application/rtf::rtf", "application/set::set", "application/sla::stl", "application/solids::sol", "application/STEP::stp", "application/vda::vda", "application/vnd.openxmlformats-officedocument.wordprocessingml.document::docx",
                "application/vnd.ms-word.document.macroEnabled.12::docm", "application/vnd.openxmlformats-officedocument.wordprocessingml.template::dotx", "application/vnd.ms-word.template.macroEnabled.12::dotm", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet::xlsx", "application/vnd.ms-excel.sheet.macroEnabled.12::xlsm", "application/vnd.openxmlformats-officedocument.spreadsheetml.template::xltx", "application/vnd.ms-excel.template.macroEnabled.12::xltm", "application/vnd.ms-excel.sheet.binary.macroEnabled.12::xlsb", "application/vnd.ms-excel.addin.macroEnabled.12::xlam", "application/vnd.openxmlformats-officedocument.presentationml.presentation::pptx",
                "application/vnd.ms-powerpoint.presentation.macroEnabled.12::pptm", "application/vnd.openxmlformats-officedocument.presentationml.slideshow::ppsx", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12::ppsm", "application/vnd.openxmlformats-officedocument.presentationml.template::potx", "application/vnd.ms-powerpoint.template.macroEnabled.12::potm", "application/vnd.ms-powerpoint.addin.macroEnabled.12::ppam", "application/vnd.openxmlformats-officedocument.presentationml.slide::sldx", "application/vnd.ms-powerpoint.slide.macroEnabled.12::sldm", "application/msonenote::one", "application/msonenote::onetoc2",
                "application/msonenote::onetmp", "application/msonenote::onepkg", "application/vnd.ms-officetheme::thmx", "application/x-bcpio::bcpio", "application/x-cpio::cpio", "application/x-csh::csh", "application/x-director::dxr", "application/x-dvi::dvi", "application/x-dwf::dwf", "application/x-gtar::gtar",
                "application/x-gzip::gzip", "application/x-hdf::hdf", "application/x-javascript::js", "application/x-latex::latex", "application/x-macbinary::bin", "application/x-midi::mid", "application/x-mif::mif", "application/x-netcdf::nc", "application/x-sh::sh", "application/x-shar::shar",
                "application/x-shockwave-flash::swf", "application/x-stuffit::sit", "application/x-sv4cpio::sv4cpio", "application/x-sv4crc::sv4crc", "application/x-tar::tar", "application/x-tcl::tcl", "application/x-tex::tex", "application/x-texinfo::texinfo", "application/x-troff::tr", "application/x-troff-man::man",
                "application/x-troff-me::me", "application/x-troff-ms::ms", "application/x-ustar::ustar", "application/x-wais-source::src", "application/x-winhelp::hlp", "application/zip::zip", "audio/basic::snd", "audio/midi::midi", "audio/x-aiff::aiff", "audio/x-mpeg::mp3",
                "audio/x-pn-realaudio::ram", "audio/x-pn-realaudio-plugin::rpm", "audio/x-voice::voc", "audio/x-wav::wav", "image/bmp::bmp", "image/gif::gif", "image/ief::ief", "image/jpeg::jpg", "image/pict::pict", "image/png::png",
                "image/tiff::tiff", "image/x-cmu-raster::ras", "image/x-portable-anymap::pnm", "image/x-portable-bitmap::pbm", "image/x-portable-graymap::pgm", "image/x-portable-pixmap::ppm", "image/x-rgb::rgb", "image/x-xbitmap::xbm", "image/x-xpixmap::xpm", "image/x-xwindowdump::xwd",
                "multipart/x-gzip::gzip", "multipart/x-zip::zip", "text/html::html", "text/plain::txt", "text/richtext::rtx", "text/tab-separated-values::tsv", "text/x-setext::etx", "text/x-sgml::sgml", "video/mpeg::mpg", "video/msvideo::avi",
                "video/quicktime::qt", "video/vdo::vdo", "video/vivo::vivo", "video/x-sgi-movie::movie", "x-conference/x-cooltalk::ice", "application/x-ms-application::application", "application/x-ms-manifest::manifest", "x-world/x-svr::svr", "x-world/x-vrml::wrl", "x-world/x-vrt::vrt",
                "text/plain::ps1"};
        for (byte b = 0; b < arrstring.length; b++) {
            String[] strs1 = arrstring[b].split("::");
            if (string.endsWith(strs1[1]))
                return strs1[0];
        }
        return "application/octet-stream";
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.event = actionEvent;
        this.file = map.get("file") + "";
        this.uri = map.get("uri") + "";
        this.port = map.get("port") + "";
        this.mime = map.get("mimetype") + "";
        this.host = map.get("host") + "";
        this.ssl = DialogUtils.bool(map, "ssl");
        this.proto = this.ssl ? "https://" : "http://";
        if (!(new File(this.file)).exists()) {
            DialogUtils.showError("Hey, I can't find that file!");
            return;
        }
        if (!this.uri.startsWith("/")) {
            DialogUtils.showError("Hey, your URI needs to start with a /");
            return;
        }
        this.dialog.setVisible(false);
        if ("automatic".equals(this.mime))
            this.mime = fixMime(this.file);
        (new UploadFile(this.conn, new File(this.file), this)).start();
    }

    public void complete(String string) {
        this.conn.call("cloudstrike.host_file", CommonUtils.args(this.host, Integer.valueOf(Integer.parseInt(this.port)), Boolean.valueOf(this.ssl), this.uri, string, this.mime), this);
    }

    public void cancel() {
        this.dialog.setVisible(true);
    }

    public void result(String string, Object object) {
        String str = object + "";
        if ("success".equals(str)) {
            if (DialogUtils.isShift(this.event))
                this.dialog.setVisible(true);
            DialogUtils.startedWebService("host file", this.proto + this.host + ":" + this.port + this.uri);
        } else {
            DialogUtils.showError("Unable to start web server:\n" + str);
            this.dialog.setVisible(true);
        }
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Host File", 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("file", "");
        dialogManager.set("uri", "/download/file.ext");
        dialogManager.set("port", "80");
        dialogManager.set("mimetype", "automatic");
        dialogManager.set("host", DataUtils.getLocalIP(this.datal));
        dialogManager.file("file", "File:");
        dialogManager.text("uri", "Local URI:", 10);
        dialogManager.text("host", "Local Host:", 20);
        dialogManager.text("port", "Local Port:", 20);
        dialogManager.combobox("mimetype", "Mime Type:", new String[]{
                "automatic", "application/acad", "application/arj", "application/astound", "application/clariscad", "application/drafting", "application/dxf", "application/hta", "application/i-deas", "application/iges",
                "application/java-archive", "application/mac-binhex40", "application/msaccess", "application/msexcel", "application/mspowerpoint", "application/msproject", "application/msword", "application/mswrite", "application/octet-stream", "application/oda",
                "application/pdf", "application/postscript", "application/pro_eng", "application/rtf", "application/set", "application/sla", "application/solids", "application/STEP", "application/vda", "application/x-bcpio",
                "application/x-cpio", "application/x-csh", "application/x-director", "application/x-dvi", "application/x-dwf", "application/x-gtar", "application/x-gzip", "application/x-hdf", "application/x-javascript", "application/x-latex",
                "application/x-macbinary", "application/x-midi", "application/x-mif", "application/x-netcdf", "application/x-sh", "application/x-shar", "application/x-shockwave-flash", "application/x-stuffit", "application/x-sv4cpio", "application/x-sv4crc",
                "application/x-tar", "application/x-tcl", "application/x-tex", "application/x-texinfo", "application/x-troff", "application/x-troff-man", "application/x-troff-me", "application/x-troff-ms", "application/x-ustar", "application/x-wais-source",
                "application/x-winhelp", "application/x-xpinstall", "application/zip", "audio/basic", "audio/midi", "audio/x-aiff", "audio/x-mpeg", "audio/x-pn-realaudio", "audio/x-pn-realaudio-plugin", "audio/x-voice",
                "audio/x-wav", "image/bmp", "image/gif", "image/ief", "image/jpeg", "image/pict", "image/png", "image/tiff", "image/x-cmu-raster", "image/x-portable-anymap",
                "image/x-portable-bitmap", "image/x-portable-graymap", "image/x-portable-pixmap", "image/x-rgb", "image/x-xbitmap", "image/x-xpixmap", "image/x-xwindowdump", "multipart/x-gzip", "multipart/x-zip", "text/html",
                "text/plain", "text/richtext", "text/tab-separated-values", "text/x-setext", "text/x-sgml", "video/mpeg", "video/msvideo", "video/quicktime", "video/vdo", "video/vivo",
                "video/x-sgi-movie", "x-conference/x-cooltalk", "x-world/x-svr", "x-world/x-vrml", "x-world/x-vrt"});
        dialogManager.checkbox_add("ssl", "SSL:", "Enable SSL", DataUtils.hasValidSSL(this.datal));
        JButton jButton1 = dialogManager.action_noclose("Launch");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-host-file");
        this.dialog.add(DialogUtils.description("Host a file through Cobalt Strike's web server"), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
