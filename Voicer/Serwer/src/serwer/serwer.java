package serwer;

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
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.DataLine.Info;
import javax.swing.*;


public class serwer {

        public static class VoiceReceiver_Serwer extends JFrame {
            ByteArrayOutputStream byteOutputStream;
            AudioFormat adFormat;
            TargetDataLine targetDataLine;
            AudioInputStream InputStream;
            SourceDataLine sourceLine;
            Graphics g;
            final JButton start_server = new JButton("Start");
            static JLabel status = new JLabel("Status ");
            final JButton stop_server = new JButton("Stop");
            private boolean isRunning = false;
            public Thread th;
            public DatagramSocket serverSocket;


            private AudioFormat getAudioFormat() {
                float sampleRate = 16000.0F;
                int sampleInbits = 16;
                int channels = 1;
                boolean signed = true;
                boolean bigEndian = false;
                return new AudioFormat(sampleRate, sampleInbits, channels, signed, bigEndian);
            }

            public static void main(String[] args) {
                (new serwer.VoiceReceiver_Serwer()).Run_Voice_Waves();
            }

            public void Run_Voice_Waves() {
                this.start_server.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        th = new Thread(new Runnable() {
                            public void run() {
                                try {
                                    serverSocket = new DatagramSocket(9786);
                                    serverSocket.setSoTimeout(200);
                                    VoiceReceiver_Serwer.this.isRunning = true;
                                    byte[] receiveData = new byte[10000];
                                    System.out.println("Start");
                                    while (VoiceReceiver_Serwer.this.isRunning) {
                                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                                        System.out.println("Inside");
                                        try {
                                            serverSocket.receive(receivePacket);
                                        } catch (Exception e) {
                                            System.out.println("Cont");
                                            continue;
                                        }
                                        System.out.println("RECEIVED: " + receivePacket.getAddress().getHostAddress() + " " + receivePacket.getPort());

                                        try {
                                            byte[] audioData = receivePacket.getData();
                                            InputStream byteInputStream = new ByteArrayInputStream(audioData);
                                            AudioFormat adFormat = VoiceReceiver_Serwer.this.getAudioFormat();
                                            VoiceReceiver_Serwer.this.InputStream = new AudioInputStream(byteInputStream, adFormat, (long) (audioData.length / adFormat.getFrameSize()));
                                            Info dataLineInfo = new Info(SourceDataLine.class, adFormat);
                                            VoiceReceiver_Serwer.this.sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                                            VoiceReceiver_Serwer.this.sourceLine.open(adFormat);
                                            VoiceReceiver_Serwer.this.sourceLine.start();
                                            Thread my_thread = new Thread(VoiceReceiver_Serwer.this.new my_thread());
                                            my_thread.start();
                                        } catch (Exception var9) {
                                            System.out.println(var9);
                                        }
                                    }

                                    serverSocket.close();
                                    System.out.println("stoped");
                                } catch (Exception var10) {
                                    var10.printStackTrace();
                                }

                            }
                        });
                        th.start();
                        status.setText("On");

                    }
                });
                this.stop_server.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        VoiceReceiver_Serwer.this.isRunning = false;
                        status.setText("Off");

                    }
                });

                this.getContentPane().add(this.start_server);
                this.getContentPane().add(this.status);
                this.getContentPane().add(this.stop_server);
                this.getContentPane().setLayout(new FlowLayout());
                this.setTitle("Voice waves");
                this.setDefaultCloseOperation(3);
                this.setSize(250, 100);
                this.getContentPane().setBackground(Color.white);
                this.setVisible(true);
                this.g = this.getGraphics();
            }

            class my_thread extends Thread {
                byte[] buf = new byte[10000];

                public void run() {
                    System.out.println("Started");
                    while (isRunning) {
                        try {
                            int counter = VoiceReceiver_Serwer.this.InputStream.read(this.buf, 0, this.buf.length);

                            if (counter == -1)
                                return;
                            if (counter > 0)
                                VoiceReceiver_Serwer.this.sourceLine.write(this.buf, 0, counter);

                        } catch (Exception e) {
                            //handle exception
                        }
                        System.out.println("Next Iteration");
                    }

                    System.out.println("Hello");
                }
            }
        }
    }
