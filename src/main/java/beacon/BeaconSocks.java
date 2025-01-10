package beacon;

import common.BeaconOutput;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import socks.BeaconProxyListener;
import socks.Mortal;
import socks.PortForward;
import socks.ReversePortForward;
import socks.SocksProxy;
import socks.SocksProxyServer;

public class BeaconSocks {
    protected Map socks = new HashMap();

    protected Map<String, List<Mortal>> servers = new HashMap();

    protected BeaconC2 controller;

    public BeaconSocks(BeaconC2 beaconC2) {
        this.controller = beaconC2;
    }

    public void notifyClients() {
        LinkedList<Map> linkedList = new LinkedList();
        synchronized (this) {
            for (Map.Entry entry : this.servers.entrySet()) {
                String str = (String) entry.getKey();
                List<Mortal> list = (List) entry.getValue();
                for (Mortal mortal : list) {
                    Map map = mortal.toMap();
                    map.put("bid", str);
                    linkedList.add(map);
                }
            }
        }
        this.controller.getCheckinListener().push("socks", linkedList);
    }

    public SocksProxy getBroker(String string) {
        synchronized (this) {
            if (this.socks.containsKey(string)) {
                return (SocksProxy) this.socks.get(string);
            }
            SocksProxy socksProxy = new SocksProxy();
            socksProxy.addProxyListener(new BeaconProxyListener());
            this.socks.put(string, socksProxy);
            return socksProxy;
        }
    }

    public void track(String string, Mortal paramMortal) {
        synchronized (this) {
            if (!this.servers.containsKey(string)) {
                this.servers.put(string, new LinkedList());
            }
            LinkedList linkedList = (LinkedList) this.servers.get(string);
            linkedList.add(paramMortal);
        }
        notifyClients();
    }

    public void pivot(String string, int n) {
        synchronized (this) {
            SocksProxyServer socksProxyServer = new SocksProxyServer(getBroker(string));
            try {
                socksProxyServer.go(n);
                track(string, socksProxyServer);
                this.controller.getCheckinListener().output(BeaconOutput.Output(string, "started SOCKS4a server on: " + n));
            } catch (IOException iOException) {
                this.controller.getCheckinListener().output(BeaconOutput.Error(string, "Could not start SOCKS4a server on " + n + ": " + iOException.getMessage()));
            }
        }
    }

    protected ReversePortForward findPortForward(String string, int n) {
        synchronized (this) {
            if (this.servers.containsKey(string)) {
                for (Mortal mortal : this.servers.get(string)) {
                    if (mortal instanceof ReversePortForward) {
                        ReversePortForward reversePortForward = (ReversePortForward) mortal;
                        if (reversePortForward.getPort() == n) {
                            return reversePortForward;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void accept(String string, int n1, int n2) {
        synchronized (this) {
            ReversePortForward reversePortForward = findPortForward(string, n1);
            if (reversePortForward == null) {
                return;
            }
            reversePortForward.accept(n2);
        }
    }

    public void portfwd(String string1, int n1, String string2, int n2) {
        synchronized (this) {
            PortForward portForward = new PortForward(getBroker(string1), string2, n2);
            try {
                portForward.go(n1);
                track(string1, portForward);
                this.controller.getCheckinListener().output(BeaconOutput.Output(string1, "started port forward on " + n1 + " to " + string2 + ":" + n2));
            } catch (IOException iOException) {
                this.controller.getCheckinListener().output(BeaconOutput.Error(string1, "Could not start port forward on " + n1 + ": " + iOException.getMessage()));
            }
        }
    }

    public void rportfwd(String string1, int n1, String string2, int n2) {
        synchronized (this) {
            ReversePortForward reversePortForward = new ReversePortForward(getBroker(string1), n1, string2, n2);
            track(string1, reversePortForward);
            this.controller.getCheckinListener().output(BeaconOutput.Output(string1, "started reverse port forward on " + n1 + " to " + string2 + ":" + n2));
        }
    }

    public void stop_port(int n) {
        synchronized (this) {
            Iterator iterator = this.servers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String string = (String) entry.getKey();
                LinkedList<Mortal> linkedList = (LinkedList) entry.getValue();
                Iterator iterator2 = linkedList.iterator();
                while (iterator2.hasNext()) {
                    Mortal mortal = (Mortal) iterator2.next();
                    if (mortal.getPort() == n) {
                        controller.getCheckinListener().output(
                                BeaconOutput.Output(string, "stopped proxy pivot on " + n));
                        mortal.die();
                        iterator2.remove();
                    }

                }
                if (linkedList.size() == 0) {
                    iterator.remove();
                }
            }
        }
        notifyClients();
    }

    public void stop(String string) {
        synchronized (this) {
            if (this.servers.containsKey(string)) {
                for (Mortal mortal : this.servers.get(string)) {
                    mortal.die();
                }
                this.servers.remove(string);
            }
        }
        this.controller.getCheckinListener().output(BeaconOutput.Output(string, "stopped SOCKS4a servers"));
        notifyClients();
    }

    public boolean isActive(String string) {
        synchronized (this) {
            return this.servers.containsKey(string);
        }
    }

    public void die(String string, int n) {
        synchronized (this) {
            SocksProxy socksProxy = (SocksProxy) this.socks.get(string);
            if (socksProxy == null) {
                return;
            }
            socksProxy.die(n);
        }
    }

    public void write(String string, int n, byte[] arrby) {
        synchronized (this) {
            SocksProxy socksProxy = (SocksProxy) this.socks.get(string);
            if (socksProxy == null) {
                return;
            }
            socksProxy.write(n, arrby, 0, arrby.length);
        }
    }

    public void resume(String string, int n) {
        synchronized (this) {
            SocksProxy socksProxy = (SocksProxy) this.socks.get(string);
            if (socksProxy == null) {
                return;
            }
            socksProxy.resume(n);
        }
    }

    public byte[] dump(String string, int n) {
        synchronized (this) {
            SocksProxy socksProxy = (SocksProxy) this.socks.get(string);
            if (socksProxy == null) {
                return new byte[0];
            }
            return socksProxy.grab(n);
        }
    }
}
