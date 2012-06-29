package org.sdu.dbaction;

import java.util.List;

import org.sdu.dao.BugDao;
import org.sdu.pojo.Bug;

import android.content.Context;
/**
 * 
 * @author laofeifd
 * 
 * @�������Ҫ�����Ǵ�����Bug����ص�ҵ���߼�
 */
public class BugAction  {
	private Context context; 
	private BugDao bugDao;
	public BugAction(Context context){
		this.context=context;
		bugDao=new BugDao(this.context);
	}
	/**
	 * ��������
	 * @param bug ��Ҫ��¼������
	 * @return	      �����Ƿ�ɹ�
	 */
	public boolean establishBug(Bug bug){
		return bugDao.insert(bug)>0?true:false;
	}
	/**
	 * ���ⷢ��
	 * @param bug	��Ҫ����������
	 */
	public void releaseBug(Bug bug){
		 bugDao.update(bug);
	}
	/**
	 * ��ȡ�����б�
	 * @return	���������б�
	 */
	public List<Bug> search(){
		return bugDao.find();
	}
	/**
	 * �����������ϸ��Ϣ
	 * @param bugId	��ʶ�����id
	 * @return		����һ������(bug)����
	 */
	public Bug BugDetail(int bugId){
		return bugDao.get(bugId);
	}
	/**
	 * ����ʶΪbugId���������ó��ѽ��
	 * @param bugId	����������id
	 */
	public void finish(int bugId){
		Bug bug=bugDao.get(bugId);
		bug.setState("1");
	}
	
}