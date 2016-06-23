
package org.kaapi.app.services;

import java.util.List;

import org.kaapi.app.entities.Pagination;
import org.kaapi.app.entities.User;
import org.kaapi.app.forms.FrmAddUpdateCoverPhoto;
import org.kaapi.app.forms.FrmAddUser;
import org.kaapi.app.forms.FrmChangePassword;
import org.kaapi.app.forms.FrmMobileLogin;
import org.kaapi.app.forms.FrmMobileRegister;
import org.kaapi.app.forms.FrmResetPassword;
import org.kaapi.app.forms.FrmUpdateUser;
import org.kaapi.app.forms.FrmValidateEmail;
import org.kaapi.app.forms.FrmWebLogin;
import org.kaapi.app.forms.accountSetting;

public interface UserService {

	
	public User mobileLogin(FrmMobileLogin mFrm);
	public User webLogin(FrmWebLogin wFrm);
	public List<User> listUser(Pagination pagination);	
	public int countUser();
	public List<User> searchUserByUsername(User user,Pagination pagination);
	public int countSearchUserByUsername(User user);
	public User getUSerById(String id);
	public User getUSerEmail(String email);
	public boolean validateEmail(FrmValidateEmail email);
	public boolean insertUser(FrmAddUser user);
	public boolean mobileInsertUser(FrmMobileRegister user);
	public boolean updateUser(FrmUpdateUser user);
	public boolean deleteUser(String id);
	public boolean insertCoverPhoto(FrmAddUpdateCoverPhoto coverPhoto);
	public boolean updateCoverPhoto(FrmAddUpdateCoverPhoto coverPhoto);
	public boolean resetPassword(FrmResetPassword resetPassword);
	public boolean changePassword(FrmChangePassword changePassword);
	public boolean updateType(String userId, String typeId);
    public boolean insertHistoryResetPassWord(String id,String email,String type);
    public accountSetting getHistoryAccountSetting(String id);
    public boolean updateHistoryResetPassword(String id);
    public boolean confirmEmail(String email);
    
    public boolean checkSocialID(String scType , String ID);
	public boolean insertUserSC(FrmAddUser u);
	public boolean checkIsFacebookAccount(String email);
	
	public boolean isAccountConfirmed(String email);
	
	public boolean isUpdateUserFaceboook(FrmAddUser user);
	public boolean checkSocialID(String ID);
	public boolean checkSocialIDandEmail(String ID, String email);
	
	public boolean updateResendCountEmail(String email);

	
	
}

