package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.mapper.StudentMapper;
import com.yupi.springbootinit.model.entity.Student;
import org.springframework.stereotype.Service;

/**
 * @author YU
 * @description 针对表【student(学生信息表)】的数据库操作Service实现
 * @createDate 2026-05-20 22:08:06
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student>
    implements IService<Student> {

}
