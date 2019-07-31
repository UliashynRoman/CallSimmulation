//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package client;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.DataLine.Info;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

class VUClient extends JFrame {
    boolean stopaudioCapture = false;
    ByteArrayOutputStream byteOutputStream;
    AudioFormat adFormat;
    TargetDataLine targetDataLine;
    AudioInputStream InputStream;
    SourceDataLine sourceLine;
    Graphics g;
    Thread captureThread;
    public String IP = "";
    public int port;
    public final JTextField port_text = new JTextField("8189");
    public final JTextField connect_text = new JTextField("192.168.0.80");

    public static void main(String[] args) {
        new VUClient();
    }

    public VUClient() {
        final JButton call = new JButton("Call");
        final JButton stop = new JButton("Stop");
        final JButton play = new JButton("Playback");
        final JButton connect = new JButton("Connect");
        connect.setEnabled(true);
        call.setEnabled(false);
        stop.setEnabled(false);
        play.setEnabled(false);
        connect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                VUClient.this.IP = VUClient.this.connect_text.getText();
                port = Integer.parseInt(port_text.getText());

                connect.setEnabled(false);
                call.setEnabled(true);
                stop.setEnabled(false);
                play.setEnabled(false);
            }
        });
        this.getContentPane().add(connect);
        call.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                call.setEnabled(false);
                stop.setEnabled(true);
                play.setEnabled(false);
                connect.setEnabled(false);
                VUClient.this.captureAudio();
            }
        });
        this.getContentPane().add(call);
        stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                call.setEnabled(true);
                stop.setEnabled(false);
                play.setEnabled(true);
                connect.setEnabled(true);
                VUClient.this.stopaudioCapture = true;
                VUClient.this.targetDataLine.close();
            }
        });
        this.getContentPane().add(stop);
        play.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                VUClient.this.playAudio();
            }
        });
        this.getContentPane().add(play);
        this.getContentPane().add(this.connect_text);
        this.getContentPane().add(this.port_text);

        this.getContentPane().setLayout(new FlowLayout());
        this.setTitle("Voice waves");
        this.setDefaultCloseOperation(3);
        this.setSize(390, 120);
        this.getContentPane().setBackground(Color.white);
        this.setVisible(true);
        this.g = this.getGraphics();
    }

    private void captureAudio() {
        try {
            this.adFormat = this.getAudioFormat();
            Info dataLineInfo = new Info(TargetDataLine.class, this.adFormat);
            this.targetDataLine = (TargetDataLine)AudioSystem.getLine(dataLineInfo);
            this.targetDataLine.open(this.adFormat);
            this.targetDataLine.start();
            this.captureThread = new Thread(new VUClient.CaptureThread());
            this.captureThread.start();
        } catch (Exception e) {
            StackTraceElement[] stackEle = e.getStackTrace();
            StackTraceElement[] var3 = stackEle;
            int var4 = stackEle.length;

            for(int i = 0; i < var4; ++i) {
                StackTraceElement val = var3[i];
                System.out.println(val);
            }
        }

    }

    private void playAudio() {
        try {
            byte[] audioData = this.byteOutputStream.toByteArray();
            InputStream byteInputStream = new ByteArrayInputStream(audioData);
            AudioFormat adFormat = this.getAudioFormat();
            this.InputStream = new AudioInputStream(byteInputStream, adFormat, (long)(audioData.length / adFormat.getFrameSize()));
            Info dataLineInfo = new Info(SourceDataLine.class, adFormat);
            this.sourceLine = (SourceDataLine)AudioSystem.getLine(dataLineInfo);
            this.sourceLine.open(adFormat);
            this.sourceLine.start();
            Thread playThread = new Thread(new VUClient.PlayThread());
            playThread.start();
        } catch (Exception e) {
            System.out.println(e);
            this.connect_text.setText("Error");
        }

    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleInbits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleInbits, channels, signed, bigEndian);
    }

    class PlayThread extends Thread {
        byte[] tempBuffer = new byte[10000];

        PlayThread() {
        }

        public void run() {
            while(true) {
                try {
                    int cnt;
                    if ((cnt = VUClient.this.InputStream.read(this.tempBuffer, 0, this.tempBuffer.length)) != -1) {
                        if (cnt > 0) {
                            VUClient.this.sourceLine.write(this.tempBuffer, 0, cnt);
                        }
                        continue;
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    System.exit(0);
                }

                return;
            }
        }
    }

    class CaptureThread extends Thread {
        byte[] tempBuffer = new byte[10000];
        InetAddress IPAddress;
        DatagramSocket clientSocket;

        CaptureThread() {
        }

        public void run() {
            VUClient.this.byteOutputStream = new ByteArrayOutputStream();
            VUClient.this.stopaudioCapture = false;

            try {
                this.clientSocket = new DatagramSocket(port);
                this.IPAddress = InetAddress.getByName(VUClient.this.IP);
                VUClient.this.stopaudioCapture = !this.IPAddress.isReachable(500);
                if (VUClient.this.stopaudioCapture) {
                    VUClient.this.connect_text.setText("Adress is unreachable");
                }

                while(!VUClient.this.stopaudioCapture) {
                    int cnt = VUClient.this.targetDataLine.read(this.tempBuffer, 0, this.tempBuffer.length);
                    if (cnt > 0) {
                        DatagramPacket sendPacket = new DatagramPacket(this.tempBuffer, this.tempBuffer.length, this.IPAddress, 9786);
                        this.clientSocket.send(sendPacket);
                        VUClient.this.byteOutputStream.write(this.tempBuffer, 0, cnt);
                    }
                }

                VUClient.this.stopaudioCapture = false;
                this.clientSocket.close();
                System.out.println("cp stoped");
                VUClient.this.byteOutputStream.close();
            } catch (Exception e) {
                System.out.println("CaptureThread::run()" + e);
                VUClient.this.connect_text.setText("Address in use");
                VUClient.this.stopaudioCapture = false;
            }

        }
    }
}
