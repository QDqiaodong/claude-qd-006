<template>
  <div>
    <el-card shadow="never">
      <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
        <el-date-picker v-model="query.date" type="date" value-format="YYYY-MM-DD" placeholder="按日期" clearable style="width:160px" />
        <el-select v-model="query.status" placeholder="按状态" clearable style="width:140px">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-input v-model="query.team" placeholder="班组" clearable style="width:140px" />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button type="success" @click="openSlot">排一条</el-button>
        <span style="margin-left:auto;color:#909399">共 {{ rows.length }} 条</span>
      </div>
    </el-card>

    <el-table :data="rows" border stripe size="small" style="margin-top:12px" v-loading="loading">
      <el-table-column prop="slotNo" label="排期号" width="110" />
      <el-table-column label="操作间" width="140">
        <template #default="{ row }">{{ kitchenName(row.kitchenId) }}</template>
      </el-table-column>
      <el-table-column prop="serveDate" label="日期" width="115" />
      <el-table-column prop="meal" label="餐次" width="80" />
      <el-table-column prop="team" label="班组" width="100" />
      <el-table-column label="时段" width="130">
        <template #default="{ row }">{{ hm(row.startMin) }}-{{ hm(row.endMin) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="tagType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === '待开始'" link type="primary" @click="advance(row, 'start')">开工</el-button>
          <el-button v-if="row.status === '进行中'" link type="success" @click="advance(row, 'done')">收工</el-button>
          <el-button
            v-if="row.status === '待开始' || row.status === '进行中'"
            link
            type="danger"
            @click="cancel(row)"
          >取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" title="排一条" width="500px">
      <el-form label-width="100px">
        <el-form-item label="操作间">
          <el-select v-model="form.kitchenId" placeholder="只列在用的操作间" style="width:100%">
            <el-option
              v-for="k in usableKitchens"
              :key="k.id"
              :label="`${k.name}（${k.kind}）`"
              :value="k.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="form.serveDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="餐次">
          <el-radio-group v-model="form.meal">
            <el-radio v-for="m in ['早餐', '午餐', '晚餐']" :key="m" :label="m">{{ m }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="班组"><el-input v-model="form.team" /></el-form-item>
        <el-form-item label="开始时间">
          <el-time-select v-model="form.start" start="00:00" step="00:30" end="23:30" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-select v-model="form.end" start="00:00" step="00:30" end="23:30" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">排下去</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { kitchenApi, slotApi } from '../api'

const statuses = ['待开始', '进行中', '已完成', '已取消']

const rows = ref([])
const kitchens = ref([])
const loading = ref(false)
const query = reactive({ date: '', status: '', team: '' })

const visible = ref(false)
const form = reactive({
  kitchenId: null, serveDate: '', meal: '午餐', team: '', start: '07:00', end: '09:00'
})

const usableKitchens = computed(() => kitchens.value.filter((k) => k.status === '在用'))

const hm = (min) => `${String(Math.floor(min / 60)).padStart(2, '0')}:${String(min % 60).padStart(2, '0')}`
const toMin = (t) => Number(t.slice(0, 2)) * 60 + Number(t.slice(3, 5))
const kitchenName = (id) => (id ? kitchens.value.find((k) => k.id === id)?.name || `#${id}` : '未选')
const tagType = (s) =>
  s === '已完成' ? 'success' : s === '进行中' ? 'warning' : s === '已取消' ? 'info' : ''

const load = async () => {
  loading.value = true
  try {
    rows.value = await slotApi.list({
      date: query.date || undefined,
      status: query.status || undefined,
      team: query.team || undefined
    })
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

const openSlot = () => {
  Object.assign(form, {
    kitchenId: null,
    serveDate: new Date().toISOString().slice(0, 10),
    meal: '午餐', team: '', start: '07:00', end: '09:00'
  })
  visible.value = true
}

const submit = async () => {
  if (!form.start || !form.end) {
    ElMessage.warning('请把时段选完整')
    return
  }
  try {
    await slotApi.open({
      kitchenId: form.kitchenId,
      serveDate: form.serveDate,
      meal: form.meal,
      team: form.team,
      startMin: toMin(form.start),
      endMin: toMin(form.end)
    })
    ElMessage.success('排上了')
    visible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const advance = async (row, action) => {
  try {
    await slotApi.advance(row.id, action)
    ElMessage.success('已更新')
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const cancel = async (row) => {
  try {
    await ElMessageBox.confirm(`确定取消排期 ${row.slotNo}？`, '提示')
  } catch {
    return
  }
  advance(row, 'cancel')
}

onMounted(async () => {
  try {
    kitchens.value = await kitchenApi.list({})
  } catch (e) {
    ElMessage.error(e.message)
  }
  query.date = new Date().toISOString().slice(0, 10)
  await load()
})
</script>
