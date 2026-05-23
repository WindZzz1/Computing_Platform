package generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author YU
 * @description 针对表【student(学生信息表)】的数据库操作Mapper
 * @createDate 2026-05-20 22:08:06
 * @Entity com.yupi.springbootinit.model.entity.Student
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {



}