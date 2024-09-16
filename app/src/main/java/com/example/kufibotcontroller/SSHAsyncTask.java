package com.example.kufibotcontroller;
import android.os.AsyncTask;
import android.widget.TextView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class SSHAsyncTask extends AsyncTask<String, Void, String> {

    private Session session;
    private TextView cliOutput;

    public SSHAsyncTask(Session session, TextView cliOutput) {
        this.session = session;
        this.cliOutput = cliOutput;
    }

    @Override
    protected String doInBackground(String... commands) {
        String command = commands[0];
        StringBuilder output = new StringBuilder();

        try {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            channel.setInputStream(null);
            InputStream in = channel.getInputStream();
            channel.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append("> ").append(line).append("\n");
            }
            channel.disconnect();
        } catch (Exception e) {
            output.append("Error: ").append(e.getMessage());
        }
        return output.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        cliOutput.append(result);
    }
}