package aggressor;

import aggressor.bridges.AggressorBridge;
import aggressor.bridges.AliasManager;
import aggressor.bridges.ArtifactBridge;
import aggressor.bridges.AttackBridge;
import aggressor.bridges.BeaconBridge;
import aggressor.bridges.BeaconTaskBridge;
import aggressor.bridges.CovertVPNBridge;
import aggressor.bridges.DataBridge;
import aggressor.bridges.DialogBridge;
import aggressor.bridges.ElevateBridge;
import aggressor.bridges.ElevatorBridge;
import aggressor.bridges.EventLogBridge;
import aggressor.bridges.GraphBridge;
import aggressor.bridges.LateralBridge;
import aggressor.bridges.ListenerBridge;
import aggressor.bridges.PreferencesBridge;
import aggressor.bridges.RemoteExecBridge;
import aggressor.bridges.ReportingBridge;
import aggressor.bridges.SafeDialogBridge;
import aggressor.bridges.SecureShellAliasManager;
import aggressor.bridges.SiteBridge;
import aggressor.bridges.TabBridge;
import aggressor.bridges.TeamServerBridge;
import aggressor.bridges.ToolBarBridge;
import aggressor.bridges.UtilityBridge;
import beacon.BeaconCommands;
import beacon.BeaconElevators;
import beacon.BeaconExploits;
import beacon.BeaconRemoteExecMethods;
import beacon.BeaconRemoteExploits;
import beacon.SecureShellCommands;
import common.Callback;
import common.CommonUtils;
import common.DisconnectListener;
import common.License;
import common.MudgeSanity;
import common.PlaybackStatus;
import common.TeamQueue;
import common.TeamSocket;
import console.Activity;
import cortana.Cortana;
import cortana.gui.ScriptableApplication;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Stack;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import report.ReportEngine;
import ui.KeyBindings;
import ui.KeyHandler;

public class AggressorClient extends JComponent implements ScriptableApplication, DisconnectListener, Callback {

    protected KeyBindings keys = new KeyBindings();

    protected JMenuBar menu = new JMenuBar();

    protected TabManager tabs = null;

    protected Cortana engine = null;

    protected JSplitPane split = null;

    protected String title = "Cobalt Strike";

    protected MultiFrame window = null;

    protected JSplitPane split2 = null;

    protected JToolBar tool = new JToolBar();

    protected TeamQueue conn = null;

    protected DataManager data = null;

    protected ReportEngine reports = null;

    protected AliasManager aliases = null;

    protected boolean connected = true;

    protected SecureShellAliasManager ssh_aliases = null;

    protected JPanel viz = new JPanel();

    protected CardLayout viz_c = new CardLayout();

    public String getTitle() {
        if (License.isTrial()) {
            return this.title + " (Trial)";
        }
        return this.title;
    }

    public MultiFrame getWindow() {
        return this.window;
    }

    public ReportEngine getReportEngine() {
        return this.reports;
    }

    public void showViz(String string) {
        this.viz_c.show(this.viz, string);
        this.viz.validate();
    }

    public void addViz(String string, JComponent jComponent) {
        this.viz.add(jComponent, string);
    }

    public void setTitle(String string) {
        this.window.setTitle(this, string);
    }

    public Cortana getScriptEngine() {
        return this.engine;
    }

    public TabManager getTabManager() {
        return this.tabs;
    }

    public TeamQueue getConnection() {
        return this.conn;
    }

    public DataManager getData() {
        return this.data;
    }

    public KeyBindings getBindings() {
        return this.keys;
    }

    public SecureShellAliasManager getSSHAliases() {
        return this.ssh_aliases;
    }

    public AliasManager getAliases() {
        return this.aliases;
    }

    @Override
    public void bindKey(String string, KeyHandler keyHandler) {
        this.keys.bind(string, keyHandler);
    }

    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public JMenuBar getJMenuBar() {
        return this.menu;
    }

    public void touch() {
        Component component = this.tabs.getTabbedPane().getSelectedComponent();
        if (component == null) {
            return;
        }
        if (component instanceof Activity) {
            ((Activity) component).resetNotification();
        }
        component.requestFocusInWindow();
    }

    public void kill() {
        CommonUtils.print_info("shutting down client");
        this.tabs.stop();
        this.engine.getEventManager().stop();
        this.engine.stop();
        this.conn.close();
    }

    @Override
    public void disconnected(TeamSocket paramTeamSocket) {
        disconnected();
    }

    public void disconnected() {
        JButton jButton = new JButton("Close");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                AggressorClient.this.window.quit();
            }
        });
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        jPanel.setBackground(Color.RED);
        jPanel.add(new JLabel("<html><body><strong>Disconnected from server</strong></body></html>"), "Center");
        jPanel.add(jButton, "East");
        jPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(jPanel, "South");
        revalidate();
        getData().dead();
        this.connected = false;
    }

    public void dock(JComponent jComponent, Dimension dimension) {
        this.split2.setBottomComponent(jComponent);
        this.split2.setDividerSize(10);
        this.split2.setResizeWeight(1.0D);
        jComponent.setPreferredSize(dimension);
        jComponent.setSize(dimension);
        validate();
    }

    public void noDock() {
        this.split2.setBottomComponent(null);
        this.split2.setDividerSize(0);
        this.split2.setResizeWeight(1.0D);
        validate();
    }

    public void result(String string, Object object) {
        if ("server_error".equals(string)) {
            CommonUtils.print_error("Server error: " + object);
        }
    }

    public void loadScripts() {
        try {
            this.engine.loadScript("scripts/default.cna", CommonUtils.resource("scripts/default.cna"));
        } catch (Exception exception) {
            MudgeSanity.logException("Loading scripts/default.cna", exception, false);
        }
        for (String str : Prefs.getPreferences().getList("cortana.scripts")) {
            try {
                this.engine.loadScript(str);
            } catch (Exception exception) {
                MudgeSanity.logException("Loading " + str, exception, true);
            }
        }
    }

    public AggressorClient(MultiFrame multiFrame, TeamQueue teamQueue, Map map, Map map2) {
        setup(multiFrame, teamQueue, map, map2);
    }

    public boolean isHeadless() {
        return false;
    }

    public AggressorClient() {
    }

    public void setup(MultiFrame multiFrame, TeamQueue teamQueue, Map map, Map map2) {
        this.window = multiFrame;
        this.conn = teamQueue;
        teamQueue.addDisconnectListener(this);
        this.tabs = new TabManager(this);
        this.engine = new Cortana(this);
        this.reports = new ReportEngine(this);
        this.aliases = new AliasManager();
        this.ssh_aliases = new SecureShellAliasManager();
        this.engine.register(new TabBridge(this.engine, this.tabs));
        this.engine.register(new GraphBridge(this.engine, this.tabs));
        this.engine.register(new AggressorBridge(this, this.engine, this.tabs, multiFrame, teamQueue));
        this.engine.register(new ToolBarBridge(this.tool));
        this.engine.register(new TeamServerBridge(this.engine, teamQueue));
        this.engine.register(new DataBridge(this, this.engine, teamQueue));
        this.engine.register(new BeaconBridge(this, this.engine, teamQueue));
        this.engine.register(new BeaconTaskBridge(this));
        this.engine.register(new ElevateBridge(this));
        this.engine.register(new ElevatorBridge(this));
        this.engine.register(new LateralBridge(this));
        this.engine.register(new RemoteExecBridge(this));
        this.engine.register(new UtilityBridge(this));
        this.engine.register(new ReportingBridge(this));
        this.engine.register(new EventLogBridge(this));
        this.engine.register(new SafeDialogBridge(this));
        this.engine.register(new PreferencesBridge(this));
        this.engine.register(new ListenerBridge(this));
        this.engine.register(new CovertVPNBridge(this));
        this.engine.register(new ArtifactBridge(this));
        this.engine.register(new DialogBridge(this));
        this.engine.register(new SiteBridge(this));
        this.engine.register(new AttackBridge());
        this.engine.register(this.aliases.getBridge());
        this.engine.register(this.ssh_aliases.getBridge());
        this.reports.registerInternal("scripts/default.rpt");
        this.data = new DataManager(this.engine);
        this.data.put("metadata", map);
        this.data.put("options", map2);
        this.data.put("beacon_commands", new BeaconCommands());
        this.data.put("beacon_exploits", new BeaconExploits());
        this.data.put("beacon_elevators", new BeaconElevators());
        this.data.put("beacon_remote_exploits", new BeaconRemoteExploits());
        this.data.put("beacon_remote_exec_methods", new BeaconRemoteExecMethods());
        DataUtils.getBeaconExploits(this.data).registerDefaults(this);
        DataUtils.getBeaconElevators(this.data).registerDefaults(this);
        DataUtils.getBeaconRemoteExploits(this.data).registerDefaults(this);
        DataUtils.getBeaconRemoteExecMethods(this.data).registerDefaults(this);
        this.data.put("ssh_commands", new SecureShellCommands());
        teamQueue.setSubscriber(this.data);
        this.data.subscribe("server_error", this);
        this.tabs.start();
        loadScripts();
        this.viz.setLayout(this.viz_c);
        this.split2 = new JSplitPane(0, this.tabs.getTabbedPane(), null);
        this.split2.setDividerSize(0);
        this.split2.setOneTouchExpandable(true);
        this.split = new JSplitPane(0, this.viz, this.split2);
        this.split.setOneTouchExpandable(true);
        this.tool.setFloatable(false);
        this.tool.add(Box.createHorizontalGlue());
        add(this.split, "Center");
        if (Prefs.getPreferences().isSet("client.toolbar.boolean", true)) {
            JPanel jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());
            jPanel.add(this.menu, "North");
            jPanel.add(this.tool, "Center");
            setLayout(new BorderLayout());
            add(jPanel, "North");
            add(this.split, "Center");
        } else {
            setLayout(new BorderLayout());
            add(this.menu, "North");
            add(this.split, "Center");
        }
        if (!isHeadless()) {
            new SyncMonitor(this);
        }
        getData().subscribe("playback.status", new Callback() {
            public void result(String string, Object object) {
                PlaybackStatus playbackStatus = (PlaybackStatus) object;
                if (playbackStatus.isDone()) {
                    if (AggressorClient.this.isHeadless()) {
                        GlobalDataManager.getGlobalDataManager().wait(AggressorClient.this.getData());
                    }
                    AggressorClient.this.engine.getEventManager().fireEvent("ready", new Stack());
                    AggressorClient.this.engine.go();
                }
            }
        });
        teamQueue.call("aggressor.ready");
    }

    public void showTime() {
        CommonUtils.Guard();
        this.split.setDividerLocation(0.5D);
    }
}
