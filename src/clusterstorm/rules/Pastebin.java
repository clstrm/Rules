package clusterstorm.rules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Pastebin {

	private File dir;
	
	public Pastebin() {
		dir = new File(Rules.getInstance().getDataFolder() + File.separator + "cache");
		if(!dir.isDirectory()) dir.mkdirs();
	}
	
	
	public int reload() {
		synchronized (Pastebin.class) {
			int a = 0;
			for (File f : dir.listFiles()) {
				String id = f.getName();
				id = id.substring(0, id.lastIndexOf('.'));
				List<String> payload = load(id);
				try {
					FileWriter writer = new FileWriter(f);
					for (String p : payload) {
						writer.write(p + "\n");
					}
					writer.close();
					a++;
				} catch(Exception e) {
					throw new RuntimeException("Failed to write file " + id + ".txt", e);
				}
			}
			return a;
		}
	}
	
	public List<String> getList(String id) {
		synchronized (Pastebin.class) {
			File file = new File(dir, id + ".txt");
			if(file.exists()) {
				List<String> payload = new ArrayList<>();
				Scanner s;
				try {
					s = new Scanner(file);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
				while(s.hasNextLine()) {
					String line = s.nextLine();
					payload.add(line);
				}
				s.close();
				return payload;
			}
			
			
			List<String> payload = load(id);
			try {
				file.createNewFile();
				FileWriter writer = new FileWriter(file);
				for (String p : payload) {
					writer.write(p + "\n");
				}
				writer.close();
			} catch(Exception e) {
				throw new RuntimeException("Failed to create file " + id + ".txt", e);
			}
			
			return payload;
		}
	}


	private List<String> load(String id) {
		try {
			URL url = new URL("http://pastebin.com/raw/" + id);
			InputStream source = url.openStream();
			Scanner s = new Scanner(source, StandardCharsets.UTF_8.toString());
			
			List<String> payload = new ArrayList<>();
			while(s.hasNextLine()) {
				String line = s.nextLine();
				payload.add(line);
			}
			s.close();
			return payload;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	
}
