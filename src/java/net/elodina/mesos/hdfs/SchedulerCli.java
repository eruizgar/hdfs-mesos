package net.elodina.mesos.hdfs;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static net.elodina.mesos.hdfs.Cli.Error;
import static net.elodina.mesos.hdfs.Cli.*;

public class SchedulerCli {
    public static void handle(List<String> args, boolean help) {
        OptionParser parser = new OptionParser();
        parser.accepts("api", "Binding host:port for http/artifact server.").withRequiredArg().ofType(String.class);
        parser.accepts("master", "Mesos Master addresses.").withRequiredArg().ofType(String.class);
        parser.accepts("user", "Mesos user. Default - none").withRequiredArg().ofType(String.class);

        if (help) {
            printLine("Generic Options");

            try { parser.printHelpOn(out); }
            catch (IOException ignore) {}

            return;
        }

        OptionSet options;
        try { options = parser.parse(args.toArray(new String[args.size()])); }
        catch (OptionException e) {
            try { parser.printHelpOn(out); }
            catch (IOException ignore) {}

            printLine();
            throw new Error(e.getMessage());
        }

        Map<String, String> defaults = defaults();

        String api = (String) options.valueOf("api");
        if (api == null) api = defaults.get("api");
        if (api == null) throw new Error("api required");

        String master = (String) options.valueOf("master");
        if (master == null) master = defaults.get("master");
        if (master == null) throw new Error("master required");

        String user = (String) options.valueOf("user");
        if (user == null) user = defaults.get("user");

        Scheduler.$.config.api = api;
        Scheduler.$.config.master = master;
        Scheduler.$.config.user = user;

        Scheduler.$.run();
    }

    private static Map<String, String> defaults() {
        Map<String, String> defaults = new HashMap<>();

        File file = new File("hdfs-mesos.properties");
        if (!file.exists()) return defaults;

        Properties props = new Properties();
        try (InputStream stream = new FileInputStream(file)) { props.load(stream); }
        catch (IOException e) { throw new IOError(e); }

        for (Object name : props.keySet())
            defaults.put("" + name, props.getProperty("" + name));
        return defaults;
    }
}
