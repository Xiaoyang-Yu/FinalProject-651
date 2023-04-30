<template>
  <div style="width:1366px; margin: 0 auto; padding-top: 40px">
    <div class="flexbetween">
      <div v-if="showStatus">
        <el-form :inline="true" class="demo-form-inline">
          <el-form-item label="Part ID">
            <el-input v-model="partsId" placeholder="Part ID"></el-input>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="getByKey">Query and display the next 10 items</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="getByKey('reset')">Reset</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="openNewWindow()">Add</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="warning" @click="handleExit">Exit</el-button>
          </el-form-item>
<!--          <el-form-item>
            <el-button type="primary" @click="downloadFile">下载文件</el-button>
          </el-form-item>-->
          <br/>
          <el-form-item label="The total number of splits：">
            <el-statistic group-separator="," :value="value1"></el-statistic>
          </el-form-item>
          <el-form-item label="The parent number of splits：">
            <el-statistic group-separator="," :value="value2"></el-statistic>
          </el-form-item>
          <el-form-item label="The depth of the B+ tree：">
            <el-statistic group-separator="," :value="value3"></el-statistic>
          </el-form-item>
        </el-form>
      </div>
      <!-- 新增对话框 -->
      <el-dialog @close="clearForm" :visible.sync="dialogFormVisible">
        <el-form :model="addForm" :rules="rules" ref="addFormRef" >
          <el-form-item label="Part ID" prop="partsId" :label-width="formLabelWidth">
            <el-input v-model="addForm.partsId" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="Role Description" prop="message" :label-width="formLabelWidth">
            <el-input v-model="addForm.message" autocomplete="off"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogFormVisible = false">Cancel</el-button>
          <el-button type="primary" @click="saveParts">Sure</el-button>
        </div>
      </el-dialog>

      <el-upload
          style="margin-top: 10px"
          class="upload-demo"
          action="#"
          :on-preview="handlePreview"
          :on-remove="handleRemove"
          :before-remove="beforeRemove"
          :on-change="handleChange"
          multiple
          :auto-upload="false"
          :on-exceed="handleExceed"
          :file-list="fileList">
        <el-button size="small" type="primary" v-if="showButton" @click="showButton = false">Upload</el-button>

      </el-upload>
    </div>
    <el-table
        :data="list"
        border
        style="width: 100%">

      <el-table-column
          label="#"
          type="index"
          align="center"
          width="50">
      </el-table-column>

      <el-table-column
          prop="id"
          label="ID"
          align="center"
          width="300">
      </el-table-column>

      <el-table-column
          prop="message"
          label="Description"
          align="center"
          width="765">
      </el-table-column>

      <el-table-column
          label="Operate"
          align="center"
          width="250">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="open(scope.row)">Edit</el-button>
          <el-button @click="deleteByKey(scope.row)" type="text" size="small">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="PageWrap_List">
      <el-pagination @size-change="sizeChange"
                     @current-change="currentchange_list"
                     :current-page="currentpage_list"
                     :page-sizes="[11]"
                     :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper"
                     :total="totalCount_list">
      </el-pagination>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    var checkPartsId = (rule, value, callback) => {
      // 正则表达式验证id格式
      var reg = /^[A-Z]{3}-\d{3}$/
      if (!reg.test(value)) {
        return callback(new Error('The id format is incorrect'))
      }
      callback()
    }
    return {
      value1: 0,
      value2: 0,
      value3: 0,
      addForm: {},
      dialogFormVisible: false,
      formLabelWidth: '130px',
      load:false,
      list:[],
      showStatus:false,
      pageSize: 10,
      currentpage_list: 1,
      totalCount_list: 0,
      fileList: [],
      tableData: [],
      partsId:"",
      showButton: true,
      rules: {
        partsId: [
          { required: true, message: 'Please type part id.', trigger: 'blur' },
          { validator: checkPartsId, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    open(data) {
      //console.log(data)
      //当前ID:${data.id}, 当前修改的值:${data.message}
      this.$prompt(`Please enter a description of the part with ID ${data.id} that you want to modify.`, '提示', {
        confirmButtonText: 'Yes',
        cancelButtonText: 'Cancel'
      }).then(({ value }) => {
        this.$confirm('Do you want to save the data to a local file?', 'Tip', {
          confirmButtonText: 'Yes',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }).then(() => {
          this.updateDetail(data,value)
          this.$message({
            type: 'success',
            message: 'Saved successfully!'
          })
        }).catch(() => {
          this.$message({
            type: 'info',
            message: 'Canceled save!'
          })
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: 'Cancel input'
        });
      });
    },
    openNewWindow(){
      this.dialogFormVisible = true
    },
    clearForm() {
      this.addForm = {}
      this.$refs.addFormRef.clearValidate()
    },
    saveParts(){
      this.$refs.addFormRef.validate((valid) => {  //表单验证
        console.log(valid)
        if (valid) {
          this.$axios({
            method:'POST',
            url:`${this.$apiUrl}/yxy/parts/add`,
            data:{
              key: this.addForm.partsId,
              value: this.addForm.message
            }
          }).then(res=>{
            // 关闭对话框
            this.dialogFormVisible = false
            // 刷新表格
            this.list = res.data.list
            this.getByKey()
            // 显示对话框，询问是否保存到本地文件
            this.$confirm('Add successfully, whether to save to local file？', 'Tip', {
              confirmButtonText: 'Save',
              cancelButtonText: 'Cancel',
              type: 'success'
            }).then(() => {
              // 成功保存后提示
              this.$message({
                type: 'success',
                message: 'Saved successfully!'
              });
            }).catch(() => {
              // 取消保存时提示
              this.$message({
                type: 'info',
                message: 'Unsaved'
              });
            });
          }).catch(error=>{
            console.log(error)
            // 添加错误提示信息
            this.$message({
              type: 'error',
              message: 'Failed to add, please try again.'
            });
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    handleRemove (file, fileList) {
      console.log(file, fileList)
    },
    handleChange (param) {
      this.load = true
      this.fileList = []
      // 组装数据
      const formData = new FormData()
      formData.append('file', param.raw)
      console.log(param)
      const isJPG = param.raw.type === 'text/plain'
      const isLt2M = param.raw.size / 1024 / 1024 < 2
      if (!isJPG) {
        this.$message.error('Upload txt only')
        this.fileList = []
      } else if (!isLt2M) {
        this.$message.error('Size cannot exceed 2MB!')
        this.fileList = []
      } else {
        this.load = true
        this.fileList = []
        // 组装传参数据
        // 请求后端接口
        this.$axios({
          method:'POST',
          url:`${this.$apiUrl}/yxy/parts/importTxt`,
          data:formData,
        }).then(res => {
          console.log(res)
          this.load = false
          //this.list=[]
          console.log(res.data.dataList)
          this.list = res.data.dataList
          this.showStatus = true
          // 在文件上传成功后调用后台getTenValueByKey路径获取数据
          this.$axios({
            method:'GET',
            url:`${this.$apiUrl}/yxy/parts/getTenValueByKey`,
          }).then(res => {
            console.log(res)
            this.list = res.data.dataList
            this.$axios.get(`${this.$apiUrl}/yxy/parts/getSplitCount`).then(res => {
              this.value1 = res.data[0];
              this.value2 = res.data[1];
              this.value3 = res.data[2];
            }).catch(error => {
              console.log(error);
            });
          }).catch(() => {
            this.$message.error('Failed to refresh the list')
          })
          // 附件展示的数据
        }).catch(() => {
          this.load = false
          this.$message.error('Upload failed')
        })
      }
    },
    handlePreview (file) {
      console.log(this.fileList, file)
    },
    // eslint-disable-next-line no-unused-vars
    handleExceed (files, fileList) {
      this.$message.warning(`Only one file is allowed to be uploaded!`)
    },
    // eslint-disable-next-line no-unused-vars
    beforeRemove (file, fileList) {
      return this.$confirm(`Determine removal ${file.name}？`)
    },
    currentchange_list (page) {
      this.currentpage_list = page
      this.getByKey()
    },
    sizeChange (size) {
      // 修改每页显示的数据条数
      this.pageSize = size
      // 修改当前页码为第一页
      this.currentpage_list = 1
      // 刷新表格数据
      this.getByKey()
    },
    getByKey(val){
      if(val ==='reset'){
        this.partsId = ''
      }
      this.$axios({
        method:'Get',
        url:`${this.$apiUrl}/yxy/parts/getTenValueByKey?key=${this.partsId}`,
        data:{
          key:this.partsId
        }
      }).then(res=>{
        console.log(res)
        this.load = false
        this.list=[]
        console.log(res.data.dataList)
        this.list = res.data.dataList
        if (this.list.length === 0 && this.partsId) {
          this.$message.warning('The query result is empty, please make sure the query ID is correct')
        }
        if (!this.partsId) {
          // 在文件上传成功后调用后台getTenValueByKey路径获取数据
          this.$axios({
            method:'GET',
            url:`${this.$apiUrl}/yxy/parts/getTenValueByKey`,
          }).then(res => {
            console.log(res)
            this.list = res.data.dataList
          }).catch(() => {
            this.$message.error('Failed to refresh the list')
          })
        }
        // 附件展示的数据
      }).catch(error=>{
        console.log(error)
      })
    },
    updateDetail(data,value){
      this.$axios({
        method:'post',
        url:`${this.$apiUrl}/yxy/parts/updateDetail`,
        data:{
          key:data.id,
          value:value
        }
      }).then(res=>{
        console.log(res)
        this.list = res.data.list
        // 在文件上传成功后调用后台getTenValueByKey路径获取数据
        this.$axios({
          method:'GET',
          url:`${this.$apiUrl}/yxy/parts/getTenValueByKey`,
        }).then(res => {
          console.log(res)
          this.list = res.data.dataList
        }).catch(() => {
          this.$message.error('Failed to refresh the list')
        })
      }).catch(error=>{
        console.log(error)
      })
    },
    deleteByKey(row){
      console.log(row.id)
      this.$confirm('This operation deletes the data and saves the file, whether to continue?', 'Tip', {
        confirmButtonText: 'Yes',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        this.$axios({
          method:'POST',
          url:`${this.$apiUrl}/yxy/parts/deleteByKey`,
          data:{
            key:row.id
          }
        }).then(res=>{
          this.list = res.data.list
          this.$message({
            type: 'success',
            message: 'Deleted successfully!',
          });
          // 在文件上传成功后调用后台getTenValueByKey路径获取数据
          this.$axios({
            method:'GET',
            url:`${this.$apiUrl}/yxy/parts/getTenValueByKey`,
          }).then(res => {
            console.log(res)
            this.list = res.data.dataList
          }).catch(() => {
            this.$message.error('Failed to refresh the list')
          })
        }).catch(error=>{
          console.log(error)
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: 'Undeleted'
        })})
    },
    hideButton() {
      this.showButton = false
    },
    handleExit() {
      this.$confirm('Do you want to download the modified file?', 'Exit', {
        confirmButtonText: 'Yes',
        cancelButtonText: 'No',
        type: 'warning'
      }).then(() => {
        this.downloadFile();
        //window.location.href = 'about:blank';  浏览器兼容问题，不能直接关闭当前窗口，所以把窗口指向空白页。
      }).catch(() => {});
    },
    downloadFile() {
      console.log('download方法被调用')
      const url = `${this.$apiUrl}/yxy/parts/download`;
      const xhr = new XMLHttpRequest();
      xhr.open('GET', url, true);
      xhr.setRequestHeader('Authorization', localStorage.getItem('token'));
      xhr.responseType = 'blob';
      xhr.onload = function() {
        if (this.status === 200) {
          const filename = 'Parts-Data.txt';
          const contentType = 'application/octet-stream';
          const blob = new Blob([this.response], { type: contentType });
          const link = document.createElement('a');
          link.href = window.URL.createObjectURL(blob);
          link.download = filename;
          link.click();
          window.location.href = 'about:blank';   //下载完成后，跳转到空白页，退出当前系统
        }
      };
      xhr.send();
    }
  },
}

</script>

<style scoped>
.flexbetween{
  display: flex;
  display:-webkit-flex;
  display: -moz-box;
  display: -ms-flexbox;
  -webkit-justify-content: space-between;
}
.PageWrap_List{
  width: 100%;
  margin: 15px auto;
  text-align: left;
}
</style>
