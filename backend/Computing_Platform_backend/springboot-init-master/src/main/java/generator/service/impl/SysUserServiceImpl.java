package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.mapper.SysUserMapper;
import generator.domain.SysUser;
import generator.service.SysUserService;
import org.springframework.stereotype.Service;

/**
* @author YU
* @description 针对表【sys_user(用户表)】的数据库操作Service实现
* @createDate 2026-05-21 18:39:44
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService{

}




