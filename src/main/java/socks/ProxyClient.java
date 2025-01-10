package socks;

import java.io.IOException;
import java.net.Socket;

public class ProxyClient extends BasicClient {
    protected SocksCommand command = null;

    public ProxyClient(SocksProxy socksProxy, Socket socket, int n) {
        super(socksProxy, socket, n);
    }

    public void start() {
        try {
            this.command.reply(this.out, 90);
            this.started = true;
        } catch (IOException iOException) {
            die();
            return;
        }
        super.start();
    }

    protected void deny() {
        try {
            this.command.reply(this.out, 91);
            super.deny();
        } catch (IOException iOException) {
        }
    }

    public void run() {
        try {
            setup();
            this.command = new SocksCommand(this.in);
            if (this.command.getCommand() == 1) {
                this.parent.fireEvent(ProxyEvent.EVENT_CONNECT(this.chid, this.command.getHost(), this.command.getPort()));
            } else {
                this.parent.fireEvent(ProxyEvent.EVENT_LISTEN(this.chid, this.command.getHost(), this.command.getPort()));
            }
        } catch (IOException iOException) {
            try {
                this.client.close();
            } catch (IOException iOException1) {
            }
            return;
        }
    }
}
