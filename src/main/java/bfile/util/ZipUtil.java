package bfile.util;

import bfile.util.IOUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * 通过Java的Zip输入输出流实现压缩和解压文件
 * 
 * @author liujiduo
 * 
 */
public final class ZipUtil {

	private ZipUtil() {
		// empty
	}

	/**
	 * 压缩文件
	 * 
	 * @param filePath
	 *            待压缩的文件路径
	 * @return 压缩后的文件
	 */
	public static File zip(String filePath) {
		File target = null;
		File source = new File(filePath);
		if (source.exists()) {
			// 压缩文件名=源文件名.zip
			String zipName = source.getName() + ".zip";
			target = new File(source.getParent(), zipName);
			if (target.exists()) {
				target.delete(); // 删除旧的文件
			}
			FileOutputStream fos = null;
			ZipOutputStream zos = null;
			try {
				fos = new FileOutputStream(target);
				zos = new ZipOutputStream(new BufferedOutputStream(fos));
				// 添加对应的文件Entry
				addEntry("/", source, zos);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				IOUtil.closeQuietly(zos, fos);
			}
		}
		return target;
	}

	/**
	 * 扫描添加文件Entry
	 * 
	 * @param base
	 *            基路径
	 * 
	 * @param source
	 *            源文件
	 * @param zos
	 *            Zip文件输出流
	 * @throws IOException
	 */
	private static void addEntry(String base, File source, ZipOutputStream zos)
			throws IOException {
		// 按目录分级，形如：/aaa/bbb.txt
		String entry = base + source.getName();
		if (source.isDirectory()) {
			for (File file : source.listFiles()) {
				// 递归列出目录下的所有文件，添加文件Entry
				addEntry(entry + "/", file, zos);
			}
		} else {
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			try {
				byte[] buffer = new byte[1024 * 10];
				fis = new FileInputStream(source);
				bis = new BufferedInputStream(fis, buffer.length);
				int read = 0;
				zos.putNextEntry(new ZipEntry(entry));
				while ((read = bis.read(buffer, 0, buffer.length)) != -1) {
					zos.write(buffer, 0, read);
				}
				zos.closeEntry();
			} finally {
				IOUtil.closeQuietly(bis, fis);
			}
		}
	}

	/**
	 * 解压文件
	 * 
	 * @param filePath
	 *            压缩文件路径
	 */
	public static Map<String,String> unzip(String filePath) {
		File source = new File(filePath);
		Map<String,String> returnMap = new HashMap<String,String>();
		String filePathNew = "";
		String fileName = "";
		String machineNo = getMachineNo(source.getName());
		if (source.exists()) {
			ZipInputStream zis = null;
			BufferedOutputStream bos = null;
			try {
				zis = new ZipInputStream(new FileInputStream(source));
				ZipEntry entry = null;
				while ((entry = zis.getNextEntry()) != null
						&& !entry.isDirectory()) {
					File target = new File(source.getParent(), entry.getName());
					if (!target.getParentFile().exists()) {
						// 创建文件父目录
						target.getParentFile().mkdirs();
					}
					// 写入文件
					bos = new BufferedOutputStream(new FileOutputStream(target));
					int read = 0;
					byte[] buffer = new byte[1024 * 10];
					while ((read = zis.read(buffer, 0, buffer.length)) != -1) {
						bos.write(buffer, 0, read);
					}

					bos.flush();
					filePathNew = target.getPath();
					fileName = target.getName();
				}
				zis.closeEntry();

			} catch (IOException e) {
				returnMap.put("success","false");
				return returnMap;
			} finally {
				IOUtil.closeQuietly(zis, bos);
			}
		}
		returnMap.put("success","true");
		returnMap.put("filePathNew",filePathNew);
		returnMap.put("fileName",fileName);
		returnMap.put("machineNo",machineNo);
		return returnMap;
	}

	public static void main(String[] args) {
//		String targetPath = "E:\\Win7壁纸";
//		File file = ZipUtil.zip(targetPath);
//		System.out.println(file);
//		ZipUtil.unzip("F:\\Win7壁纸.zip");
		String fileName = "plcb_140213001_20151208_1225_000.arc";
		getMachineNo(fileName);
	}

	public static String getMachineNo(String fileName){
		String[] arrs = fileName.split("_");
		String machineNo = arrs[1];
		return machineNo;
	}

	//    public static void main(String[] args) throws IOException {
////        //ZipUtil.unzip("/home/chenjingsi/金风文件/dmf/bmf/140213/plcb_140213001_20150708_1100_000.arc");
////        Configuration conf = new Configuration();
////        FileSystem fs = FileSystem.get(conf);
////
////
////        File file = new File("/home/chenjingsi/金风文件/dmf/bmf");
////        File[] dirList = file.listFiles();
////
////        FileFilter filter = new FileFilter() {
////            @Override
////            public boolean accept(File pathname) {
////                String suffix = pathname.getName().toLowerCase();
////                if(suffix.endsWith(".arc") && (suffix.startsWith("b") || suffix.startsWith("plcb"))){
////                    return true;
////                }
////                return false;
////            }
////        };
////
////        for(File dir : dirList){
//////            File dir = new File("/home/chenjingsi/金风文件/dmf/bmf/210921");
////            File[] fileArray = dir.listFiles(filter);
////            String dirPath = dir.getName();
////            for(File f : fileArray){
////                Map<String,String> returnMap = ZipUtil.unzip(f.getPath());
////                if(returnMap.get("success").equals("true")){
////                    try {
////                        fs.copyFromLocalFile(false,true,new Path(returnMap.get("filePathNew")),new Path("/var/gw/"+dirPath+"/"+returnMap.get("machineNo")+"/"+returnMap.get("fileName")));
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                    }
////                }else{
////                    continue;
////                }
////
////            }
////        }
//    }
}